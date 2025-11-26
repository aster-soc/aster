package site.remlit.aster.route.api

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.ApiError
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.response.AuthResponse
import site.remlit.aster.common.model.type.InstanceRegistrationsType
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.entity.UserPrivateEntity
import site.remlit.aster.event.user.UserCreateEvent
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.KeyType
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.AuthService
import site.remlit.aster.service.FormatService
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.service.InviteService
import site.remlit.aster.service.KeypairService
import site.remlit.aster.service.UserService
import site.remlit.aster.service.ValidationService
import site.remlit.aster.service.ap.ApIdService
import site.remlit.aster.util.PASSWORD_HASH_COST
import site.remlit.aster.util.PASSWORD_MIN_LENGTH
import site.remlit.aster.util.model.fromEntity

internal object RegisterRoutes {
	@Serializable
	data class RegisterBody(
		val username: String,
		val password: String,
		val invite: String? = null,
	)

	fun register() =
		RouteRegistry.registerRoute {
			post("/api/register") {
				val body = call.receive<RegisterBody>()

				if (Configuration.registrations == InstanceRegistrationsType.Closed) {
					call.respond(
						status = HttpStatusCode.BadRequest,
						message = ApiError(
							message = "Registrations closed",
							callId = call.callId
						)
					)
					return@post
				}

				if (Configuration.registrations == InstanceRegistrationsType.Invite && body.invite == null) {
					call.respond(
						status = HttpStatusCode.BadRequest,
						message = ApiError(
							message = "Invite required",
							callId = call.callId
						)
					)
					return@post
				}

				if (Configuration.registrations == InstanceRegistrationsType.Invite) {
					if (body.invite.isNullOrBlank())
						throw ApiException(HttpStatusCode.BadRequest, "Invite required")

					val invite = InviteService.getByCode(body.invite)

					if (invite == null || invite.user != null)
						throw ApiException(HttpStatusCode.BadRequest, "Invite invalid")
				}

				// todo: implement invite usage

				val username = FormatService.toASCII(body.username)

				if (ValidationService.containsNonAlphanumeric(username))
					throw ApiException(HttpStatusCode.BadRequest, "Username contains non-alphanumeric characters")

				if (Configuration.reservedUsernames.contains(username))
					throw ApiException(HttpStatusCode.BadRequest, "Username reserved")

				// todo: check if username is used
				// ilike?
				// 		val existingUser = userService.get(
				//			UserTable.username like body.username.lowercase()
				//		)
				//
				//		if (existingUser != null) {
				//			call.respond(
				//				status = HttpStatusCode.BadRequest,
				//				message = ApiError(
				//					message = "Username taken",
				//					requestId = call.callId
				//				)
				//			)
				//			return@post
				//		}


				if (body.password.length < PASSWORD_MIN_LENGTH)
					throw IllegalArgumentException("Password must be at least $PASSWORD_MIN_LENGTH characters")

				val id = IdentifierService.generate()
				val hashedPassword = BCrypt.withDefaults().hashToString(PASSWORD_HASH_COST, body.password.toCharArray())

				val keypair = KeypairService.generate()

				transaction {
					UserEntity.new(id) {
						apId = ApIdService.renderUserApId(id)
						inbox = ApIdService.renderInboxApId(id)
						outbox = ApIdService.renderOutboxApId(id)
						this.username = username
						activated = Configuration.registrations != InstanceRegistrationsType.Approval
						followingUrl = ApIdService.renderFollowingApId(id)
						followersUrl = ApIdService.renderFollowersApId(id)
						publicKey = KeypairService.keyToPem(KeyType.Public, keypair)
					}
					UserPrivateEntity.new(id) {
						password = hashedPassword
						privateKey = KeypairService.keyToPem(KeyType.Private, keypair)
					}
				}

				val user = User.fromEntity(
					UserService.getById(id)
						?: throw ApiException(HttpStatusCode.NotFound)
				)

				UserCreateEvent(user).call()

				if (Configuration.registrations == InstanceRegistrationsType.Invite)
					InviteService.useInvite(body.invite!!, user.id)

				val token = AuthService.registerToken(user.id)

				call.respond(AuthResponse(token, user))
			}
		}
}
