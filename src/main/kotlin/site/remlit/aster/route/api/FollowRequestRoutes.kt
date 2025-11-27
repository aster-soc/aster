package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.util.authentication

internal object FollowRequestRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true
			) {
				get("/api/follow-requests") {
					call.respond(HttpStatusCode.NotImplemented)
				}

				post("/api/follow-request/{id}/accept") {
					call.respond(HttpStatusCode.NotImplemented)
				}

				post("/api/follow-request/{id}/reject") {
					call.respond(HttpStatusCode.NotImplemented)
				}
			}
		}
}
