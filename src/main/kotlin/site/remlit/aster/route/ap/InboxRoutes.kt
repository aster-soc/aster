package site.remlit.aster.route.ap

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.remlit.aster.model.config.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.QueueService
import site.remlit.aster.service.ap.ApValidationService
import site.remlit.aster.util.detached

internal object InboxRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			suspend fun inboxRoute(call: RoutingCall) {
				if (Configuration.pauseInbox)
					return call.respond(HttpStatusCode.Forbidden)

				val body = call.receive<ByteArray>()
				val string = call.receive<String>()

				val sender = ApValidationService.validate(call.request, body, string)
				detached { QueueService.insertInboxJob(body, sender) }

				return call.respond(HttpStatusCode.OK)
			}

			post("/inbox") { return@post inboxRoute(call) }
			post("/users/{id}/inbox") { return@post inboxRoute(call) }
		}
}
