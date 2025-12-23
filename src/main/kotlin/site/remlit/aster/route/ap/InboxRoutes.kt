package site.remlit.aster.route.ap

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.QueueService
import site.remlit.aster.service.ap.ApValidationService
import site.remlit.aster.util.detached

internal object InboxRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			post("/inbox") {
				val body = call.receive<ByteArray>()
				val string = call.receive<String>()

				val sender = ApValidationService.validate(call.request, body, string)
				detached { QueueService.insertInboxJob(body, sender) }

				call.respond(HttpStatusCode.OK)
			}

			post("/users/{id}/inbox") {
				val body = call.receive<ByteArray>()
				val string = call.receive<String>()

				val sender = ApValidationService.validate(call.request, body, string)
				detached { QueueService.insertInboxJob(body, sender) }

				call.respond(HttpStatusCode.OK)
			}
		}
}
