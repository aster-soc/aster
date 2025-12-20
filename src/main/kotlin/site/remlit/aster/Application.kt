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
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.common.model.ApiError
import site.remlit.aster.db.Database
import site.remlit.aster.event.application.ApplicationBeginShutdownEvent
import site.remlit.aster.event.application.ApplicationBeginStartEvent
import site.remlit.aster.event.application.ApplicationFinishShutdownEvent
import site.remlit.aster.event.application.ApplicationFinishStartEvent
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.ap.ApValidationException
import site.remlit.aster.model.ap.ApValidationExceptionType
import site.remlit.aster.registry.ApObjectTypeRegistry
import site.remlit.aster.registry.PluginRegistry
import site.remlit.aster.service.CommandLineService
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.service.MigrationService
import site.remlit.aster.service.SetupService
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.setJsonConfig

internal fun main(args: Array<String>) {
	if (args.isNotEmpty() && !args[0].startsWith("-")) {
		runBlocking {
			CommandLineService.execute(args)
		}
		return
	}

	ApplicationBeginStartEvent().call()

	val server = embeddedServer(Netty, Configuration.port, Configuration.host, module = Application::module)

	Runtime.getRuntime().addShutdownHook(Thread {
		Thread.currentThread().name = "ShutdownMain"
		runBlocking {
			server.stop()
			Database.dataSource.close()
		}
		ApplicationFinishShutdownEvent().call()
	})

	server.start(wait = true)
}

@ApiStatus.Internal
fun Application.module() {
	// this hook completes first, always
	Runtime.getRuntime().addShutdownHook(Thread {
		Thread.currentThread().name = "ShutdownApplication"
		this.log.info("Shutting down...")
		ApplicationBeginShutdownEvent().call()
		PluginRegistry.disableAll()
	})

	ApObjectTypeRegistry.registerInternal()

	PluginRegistry.initialize()

	setJsonConfig()

	// access connection before using it
	Database.connection

	MigrationService.isUpToDate()
	configureQueue()

	SetupService.setup()

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

	install(AutoHeadResponse)
	install(DefaultHeaders)
	install(ForwardedHeaders)

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
			if (cause is ApiException) {
				call.respond(
					cause.status,
					ApiError(
						cause.message,
						call.callId,
						cause.stackTrace.joinToString("\n")
					)
				)
			}

			if (cause is ApValidationException) {
				call.respond(
					if (cause.type == ApValidationExceptionType.Unauthorized) HttpStatusCode.Unauthorized
					else HttpStatusCode.Forbidden,
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

	ApplicationFinishStartEvent().call()
}
