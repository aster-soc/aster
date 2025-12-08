package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.generated.PartialUser
import site.remlit.aster.common.model.request.ReportRequest
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.event.user.UserEditEvent
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.NotificationService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.ReportService
import site.remlit.aster.service.RoleService
import site.remlit.aster.service.TimeService
import site.remlit.aster.service.UserService
import site.remlit.aster.service.ap.ApActorService
import site.remlit.aster.util.authenticatedUserKey
import site.remlit.aster.util.authentication
import site.remlit.aster.util.model.fromEntity
import site.remlit.aster.util.sanitizeOrNull

internal object UserRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication {
				get("/api/lookup/{handle}") {
					val handle = call.parameters.getOrFail("handle").removePrefix("@")
					val splitHandle = handle.split("@")

					val host = if (splitHandle.size > 1) splitHandle[1].ifEmpty { null } else null

					val user = UserService.get(
						UserTable.username eq splitHandle[0]
								and (UserTable.host eq host)
					)

					if (user == null || !user.activated || user.suspended ||
						(Configuration.hideRemoteContent && user.host != null && call.attributes.getOrNull(
							authenticatedUserKey
						) == null)
					) throw ApiException(HttpStatusCode.NotFound)

					call.respond(User.fromEntity(user))
				}
			}

			get("/api/user/{id}") {
				val user = UserService.getById(call.parameters.getOrFail("id"))

				if (user == null || !user.activated || user.suspended)
					throw ApiException(HttpStatusCode.NotFound)

				call.respond(
					User.fromEntity(user)
				)
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

					val partial = call.receive<PartialUser>()

					val newUser =
						transaction {
							UserEntity.findByIdAndUpdate(user.id.toString()) {
								it.displayName = sanitizeOrNull { partial.displayName }
								it.bio = sanitizeOrNull { partial.bio }
								it.location = sanitizeOrNull { partial.location }
								it.birthday = sanitizeOrNull { partial.birthday }

								it.avatar = sanitizeOrNull { partial.avatar }
								it.avatarAlt = sanitizeOrNull { partial.avatarAlt }
								it.banner = sanitizeOrNull { partial.banner }
								it.bannerAlt = sanitizeOrNull { partial.bannerAlt }

								it.locked = partial.locked ?: false
								it.automated = partial.automated ?: false
								it.discoverable = partial.discoverable ?: false
								it.indexable = partial.indexable ?: false
								it.sensitive = partial.sensitive ?: false

								it.isCat = partial.isCat ?: false
								it.speakAsCat = partial.speakAsCat ?: false

								it.updatedAt = TimeService.now()
							} ?: throw ApiException(HttpStatusCode.NotFound)
						}

					val model = User.fromEntity(newUser)
					UserEditEvent(model).call()

					return@post call.respond(HttpStatusCode.OK, model)
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

					call.respond(
						RelationshipService.mapPair(
							RelationshipService.follow(user.id.toString(), authenticatedUser.id.toString())
						)
					)
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

					if (user.host == null)
						throw ApiException(HttpStatusCode.BadRequest, "Local users can't be refetched")

					ApActorService.resolve(user.apId, true)

					val freshUser = UserService.getById(user.id.toString())
						?: throw ApiException(HttpStatusCode.NotFound)

					return@post call.respond(HttpStatusCode.OK, User.fromEntity(freshUser))
				}

				get("/api/user/{id}/relationship") {
					val user = UserService.getById(call.parameters.getOrFail("id"))

					if (user == null || !user.activated || user.suspended)
						throw ApiException(HttpStatusCode.NotFound)

					val requestingUser = call.attributes[authenticatedUserKey]

					call.respond(
						RelationshipService.mapPair(
							RelationshipService.getPair(requestingUser.id.toString(), user.id.toString())
						)
					)
				}
			}
		}
}
