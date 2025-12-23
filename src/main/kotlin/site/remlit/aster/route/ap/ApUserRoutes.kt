package site.remlit.aster.route.ap

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.ap.ApActor
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.UserService

internal object ApUserRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			get("/users/{id}") {
				val user = UserService.getById(call.parameters.getOrFail("id"))

				if (user == null || user.host != null || !user.activated || user.suspended)
					throw ApiException(HttpStatusCode.NotFound)

				call.respond(ApActor.fromEntity(user))
			}

			get("/users/{id}/followers") {
				throw ApiException(HttpStatusCode.NotImplemented)
			}

			get("/users/{id}/following") {
				throw ApiException(HttpStatusCode.NotImplemented)
			}

			get("/users/{id}/outbox") {
				throw ApiException(HttpStatusCode.NotImplemented)
			}
		}
}
