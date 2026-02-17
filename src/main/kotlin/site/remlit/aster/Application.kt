package site.remlit.aster

import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.ApiError
import site.remlit.aster.db.Database
import site.remlit.aster.event.application.ApplicationBeginShutdownEvent
import site.remlit.aster.event.application.ApplicationBeginStartEvent
import site.remlit.aster.event.application.ApplicationFinishShutdownEvent
import site.remlit.aster.event.application.ApplicationFinishStartEvent
import site.remlit.aster.event.internal.InternalRouterReloadEvent
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.ap.ApValidationException
import site.remlit.aster.model.ap.ApValidationExceptionType
import site.remlit.aster.registry.ApObjectTypeRegistry
import site.remlit.aster.registry.ApTagTypeRegistry
import site.remlit.aster.registry.PluginRegistry
import site.remlit.aster.service.CommandLineService
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.service.MigrationService
import site.remlit.aster.service.QueueService
import site.remlit.aster.service.SetupService
import site.remlit.aster.util.addShutdownHook
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.setJsonConfig
import site.remlit.effekt.effect

typealias KtorApplication = io.ktor.server.application.Application

private interface Application
private val logger = LoggerFactory.getLogger(Application::class.java)

/**
 * Entrypoint for Aster
 * */
internal fun main(args: Array<String>) {
	if (args.isNotEmpty() && !args[0].startsWith("-"))
		return runBlocking {
			CommandLineService.execute(args)
		}

	if (Configuration.debug) CommandLineService.printDebug(args)

	if (Configuration.pauseInbox) {
		logger.warn("-----------------------------------------------")
		logger.warn(" !! WARNING !! ")
		logger.warn(" You are running Aster with the inbox disabled.")
		logger.warn(" Activities will NOT be processed.")
		logger.warn("-----------------------------------------------")
	}

	ApplicationBeginStartEvent().call()

	val server = embeddedServer(Netty, Configuration.port, Configuration.host, module = KtorApplication::module)

	addShutdownHook {
		server.stop()
		Database.dataSource.close()
		ApplicationFinishShutdownEvent().call()
	}

	server.start(wait = true)
}

@ApiStatus.Internal
fun KtorApplication.module() {
	addShutdownHook {
		ApplicationBeginShutdownEvent().call()
		logger.info("Shutting down...")
		PluginRegistry.disableAll()
		QueueService.stop()
	}

	// Initializes database
	Database.connection

	MigrationService.isUpToDate()
	ApObjectTypeRegistry.registerInternal()
	ApTagTypeRegistry.registerInternal()

	setJsonConfig()

	SetupService.setup()
	PluginRegistry.initialize()

	install(DoubleReceive)
	install(AutoHeadResponse)
	install(DefaultHeaders)
	install(ForwardedHeaders)
	install(WebSockets)

	install(CallLogging) {
		filter { call ->
			!call.request.uri.startsWith("/metrics") &&
					!call.request.uri.startsWith("/assets") &&
					!call.request.uri.startsWith("/admin/assets") &&
					!call.request.uri.startsWith("/uikit") &&
					!call.request.uri.startsWith("/favicon") &&
					!call.request.uri.startsWith("/installHook.js") &&
					!call.request.uri.startsWith("/admin/installHook.js") &&
					!call.request.uri.startsWith("/manifest.json") &&
					!call.request.uri.startsWith("/robots.txt")
		}
		format { call ->
			val method = call.request.httpMethod.value
			val status = call.response.status()?.value
			val uri = call.request.uri

			"$status $method    $uri"
		}
	}

	install(CallId) {
		header(HttpHeaders.XRequestId)
		generate { IdentifierService.generate() }
	}

	install(ContentNegotiation) {
		json(jsonConfig)

		register(
			ContentType.parse("application/ld+json"),
			KotlinxSerializationConverter(jsonConfig)
		)
		register(
			ContentType.parse("application/activity+json"),
			KotlinxSerializationConverter(jsonConfig)
		)
		register(
			ContentType.parse("application/jrd+json"),
			KotlinxSerializationConverter(jsonConfig)
		)
	}

	install(StatusPages) {
		exception<Throwable> { call, cause ->
			if (Configuration.debug) cause.printStackTrace()

			if (cause is ApiException) {
				call.respond(
					cause.status,
					ApiError(
						cause.message,
						call.callId,
						cause.stackTrace.joinToString("\n")
					)
				)
				return@exception
			}

			if (cause is ApValidationException) {
				call.respond(
					when (cause.type) {
						ApValidationExceptionType.Unauthorized -> HttpStatusCode.Unauthorized
						ApValidationExceptionType.Forbidden -> HttpStatusCode.Forbidden
						ApValidationExceptionType.Ignore -> HttpStatusCode.OK
					},
					ApiError(
						cause.message,
						call.callId,
						cause.stackTrace.joinToString("\n")
					)
				)
				return@exception
			}

			if (cause is IllegalArgumentException) {
				call.respond(
					HttpStatusCode.BadRequest,
					ApiError(
						cause.message,
						call.callId,
						cause.stackTrace.joinToString("\n")
					)
				)
				return@exception
			}

			call.respond(
				HttpStatusCode.InternalServerError, ApiError(
					cause.message,
					call.callId,
					cause.stackTrace.joinToString("\n")
				)
			)
			return@exception
		}
	}

	configureRouting()

	effect<InternalRouterReloadEvent> {
		logger.warn("Unable to restart router. Please restart for routes to update.")
	}

	configureQueue()

	ApplicationFinishStartEvent().call()
}
