package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import site.remlit.aster.common.model.RelationshipPair
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.generated.PartialUser
import site.remlit.aster.common.model.request.TotpConfirmRequest
import site.remlit.aster.common.model.request.ReportRequest
import site.remlit.aster.common.model.response.RegisterTotpResponse
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.AuthService
import site.remlit.aster.service.NotificationService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.ReportService
import site.remlit.aster.service.RoleService
import site.remlit.aster.service.UserService
import site.remlit.aster.service.ap.ApActorService
import site.remlit.aster.util.authenticatedUserKey
import site.remlit.aster.util.authentication
import site.remlit.aster.util.model.fromEntity

internal object UserRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication {
				get("/api/lookup/{handle}") {
					val authenticatedUser = call.attributes.getOrNull(authenticatedUserKey)
					val handle = call.parameters.getOrFail("handle").removePrefix("@")

					val user = ApActorService.resolveHandle(handle)

					if (user == null || !user.activated || user.suspended ||
						(Configuration.hideRemoteContent && !user.isLocal() && authenticatedUser == null)
					) throw ApiException(HttpStatusCode.NotFound)

					call.respond(User.fromEntity(user))
				}
			}

			get("/api/user/{id}") {
				val authenticatedUser = call.attributes.getOrNull(authenticatedUserKey)
				val user = UserService.getById(call.parameters.getOrFail("id"))

				if (user == null || !user.activated || user.suspended ||
					(Configuration.hideRemoteContent && !user.isLocal() && authenticatedUser == null)
				) throw ApiException(HttpStatusCode.NotFound)

				call.respond(User.fromEntity(user))
			}

			authentication(
				required = true,
			) {
				post("/api/user/{id}") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val user = UserService.getById(call.parameters.getOrFail("id"))

					if (user == null || !user.activated || user.suspended)
						throw ApiException(HttpStatusCode.NotFound)

					if (user.id != authenticatedUser.id && !RoleService.isModOrAdmin(authenticatedUser.id.toString()))
						throw ApiException(HttpStatusCode.BadRequest, "You don't have permission to edit this users")

					val body = call.receive<PartialUser>()

					val updated = UserService.update(
						user,

						body.displayName,
						body.bio,
						body.location,
						body.birthday,

						body.avatar,
						body.avatarAlt,
						body.banner,
						body.bannerAlt,

						body.locked == true,
						body.automated == true,
						body.discoverable == true,
						body.indexable == true,
						body.sensitive == true,

						body.isCat == true,
						body.speakAsCat == true,
					)

					call.respond(User.fromEntity(updated))
				}

				post("/api/user/{id}/bite") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val user = UserService.getById(call.parameters.getOrFail("id"))

					if (user == null || !user.activated || user.suspended)
						throw ApiException(HttpStatusCode.NotFound)

					if (user.id == authenticatedUser.id)
						throw ApiException(HttpStatusCode.BadRequest, "You can't bite yourself")

					if (RelationshipService.eitherBlocking(user.id.toString(), authenticatedUser.id.toString()))
						throw ApiException(HttpStatusCode.Forbidden)

					NotificationService.bite(user, authenticatedUser)

					throw ApiException(HttpStatusCode.OK)
				}

				post("/api/user/{id}/follow") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val user = UserService.getById(call.parameters.getOrFail("id"))

					if (user == null || !user.activated || user.suspended)
						throw ApiException(HttpStatusCode.NotFound)

					call.respond(RelationshipService.follow(user.id.toString(), authenticatedUser.id.toString()))
				}

				post("/api/user/{id}/report") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val user = UserService.getById(call.parameters.getOrFail("id"))
					val body = call.receive<ReportRequest>()

					if (user == null || !user.activated || user.suspended)
						throw ApiException(HttpStatusCode.NotFound)

					val report = ReportService.create(
						authenticatedUser,
						body.comment,
						null,
						user.id.toString()
					)

					call.respond(report)
				}

				post("/api/user/{id}/mute") {
					val user = UserService.getById(call.parameters.getOrFail("id"))

					if (user == null || !user.activated || user.suspended)
						throw ApiException(HttpStatusCode.NotFound)

					throw ApiException(HttpStatusCode.NotImplemented)
				}

				post("/api/user/{id}/block") {
					val user = UserService.getById(call.parameters.getOrFail("id"))

					if (user == null || !user.activated || user.suspended)
						throw ApiException(HttpStatusCode.NotFound)

					throw ApiException(HttpStatusCode.NotImplemented)
				}

				post("/api/user/{id}/refetch") {
					val user = UserService.getById(call.parameters.getOrFail("id"))

					if (user == null || !user.activated || user.suspended)
						throw ApiException(HttpStatusCode.NotFound)

					if (user.isLocal())
						throw ApiException(HttpStatusCode.BadRequest, "Local users can't be refetched")

					ApActorService.resolve(user.apId, true)

					val freshUser = UserService.getById(user.id.toString())
						?: throw ApiException(HttpStatusCode.NotFound)

					call.respond(User.fromEntity(freshUser))
				}

				get("/api/user/{id}/relationship") {
					val user = UserService.getById(call.parameters.getOrFail("id"))

					if (user == null || !user.activated || user.suspended)
						throw ApiException(HttpStatusCode.NotFound)

					val requestingUser = call.attributes[authenticatedUserKey]

					val pair = RelationshipService.getPair(requestingUser.id.toString(), user.id.toString())
					call.respond(pair)
				}

				post("/api/user/totp/register") {
					val authenticatedUser = call.attributes[authenticatedUserKey]

					val private = UserService.getPrivateById(authenticatedUser.id.toString())

					if (private?.totpSecret != null)
						throw ApiException(HttpStatusCode.Conflict, "Already setup")

					call.respond(RegisterTotpResponse(
						AuthService.registerTotp(authenticatedUser.id.toString())
					))
				}

				post("/api/user/totp/confirm") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val body = call.receive<TotpConfirmRequest>()

					if (!AuthService.confirmTotp(authenticatedUser.id.toString(), body.code))
						throw ApiException(HttpStatusCode.Forbidden, "One time password incorrect")

					call.respond(HttpStatusCode.OK)
				}

				post("/api/user/totp/unregister") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					AuthService.removeTotp(authenticatedUser.id.toString())
					call.respond(HttpStatusCode.OK)
				}
			}
		}
}
