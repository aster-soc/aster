package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import site.remlit.aster.model.ApiException
import site.remlit.aster.registry.RouteRegistry

object PasswordResetRoutes {
	@Serializable
	data class PasswordResetRequest(
		val username: String,
		val code: String,
	)

	fun register() =
		RouteRegistry.registerRoute {
			post("/api/password-reset") {
				val body = call.receive<PasswordResetRequest>()
				throw ApiException(HttpStatusCode.NotImplemented)
			}
		}
}
