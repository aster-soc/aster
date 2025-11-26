package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import site.remlit.aster.common.model.response.PasswordResetCodeResponse
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.model.ApiException
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.PasswordResetService
import site.remlit.aster.service.UserService
import site.remlit.aster.util.authenticatedUserKey
import site.remlit.aster.util.authentication

object PasswordResetRoutes {
	@Serializable
	data class PasswordResetRequest(
		val password: String,
		val code: String,
	)

	@Serializable
	data class PasswordResetCodeRequest(
		val user: String,
	)

	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Mod,
			) {
				post("/api/mod/password-reset") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val body = call.receive<PasswordResetCodeRequest>()
					val user = UserService.getById(body.user)
						?: throw ApiException(HttpStatusCode.NotFound, "User not found")

					val code = PasswordResetService.createCode(
						user,
						authenticatedUser
					)

					call.respond(HttpStatusCode.OK, PasswordResetCodeResponse(code))
				}
			}

			post("/api/password-reset") {
				val body = call.receive<PasswordResetRequest>()

				PasswordResetService.resetPassword(
					body.code,
					body.password
				)

				call.respond(HttpStatusCode.OK)
			}
		}
}
