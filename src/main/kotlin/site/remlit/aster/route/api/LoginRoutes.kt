package site.remlit.aster.route.api

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.A
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.request.LoginRequest
import site.remlit.aster.common.model.request.LoginRequirementsRequest
import site.remlit.aster.common.model.response.AuthResponse
import site.remlit.aster.common.model.response.LoginRequirementsResponse
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.ApiException
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.AuthService
import site.remlit.aster.service.UserService
import site.remlit.aster.util.model.fromEntity

internal object LoginRoutes {

	fun register() =
		RouteRegistry.registerRoute {
			post("/api/login") {
				val body = call.receive<LoginRequest>()

				if (body.username.isBlank())
					throw ApiException(HttpStatusCode.BadRequest, "Username required")

				if (body.password.isBlank())
					throw ApiException(HttpStatusCode.BadRequest, "Password required")

				val user = User.fromEntity(
					UserService.get(
						UserTable.username eq body.username
								and (UserTable.host eq null)
					) ?: throw ApiException(HttpStatusCode.NotFound)
				)

				val userPrivate = UserService.getPrivateById(user.id)
					?: throw ApiException(HttpStatusCode.BadRequest, "User not found")

				val passwordValid =
					BCrypt.verifyer().verify(body.password.toCharArray(), userPrivate.password.toCharArray())

				if (!passwordValid.verified)
					throw ApiException(HttpStatusCode.BadRequest, "Incorrect password")

				if (userPrivate.totpSecret != null && body.totp == null)
					throw ApiException(HttpStatusCode.BadRequest, "One time password required")

				if (userPrivate.totpSecret != null && !AuthService.confirmTotp(user.id, body.totp!!))
					throw ApiException(HttpStatusCode.BadRequest, "One time password incorrect")

				val token = AuthService.registerToken(user.id)

				call.respond(AuthResponse(token, user))
			}

			post("/api/login/requirements") {
				val body = call.receive<LoginRequirementsRequest>()

				val private = UserService.getPrivate(UserTable.host eq null and
					(UserTable.username eq body.username))
					?: throw ApiException(HttpStatusCode.NotFound, "User not found")

				call.respond(LoginRequirementsResponse(
					totp = private.totpSecret != null,
				))
			}
		}
}
