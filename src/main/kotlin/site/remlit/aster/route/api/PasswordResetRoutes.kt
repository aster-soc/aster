package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import site.remlit.aster.model.ApiException
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.PasswordResetService
import site.remlit.aster.service.UserService

object PasswordResetRoutes {
	@Serializable
	data class PasswordResetRequest(
		val username: String,
		val password: String,
		val code: String,
	)

	fun register() =
		RouteRegistry.registerRoute {
			post("/api/password-reset") {
				val body = call.receive<PasswordResetRequest>()

				val user = UserService.getByUsername(body.username)
					?: throw ApiException(HttpStatusCode.NotFound, "User not found")

				PasswordResetService.resetPassword(
					body.code,
					user,
					body.password
				)

				throw ApiException(HttpStatusCode.NotImplemented)
			}
		}
}
