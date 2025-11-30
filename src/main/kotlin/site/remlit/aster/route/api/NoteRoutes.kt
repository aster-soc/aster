package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.BookmarkService
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.NotificationService
import site.remlit.aster.service.RoleService
import site.remlit.aster.service.VisibilityService
import site.remlit.aster.util.authenticatedUserKey
import site.remlit.aster.util.authentication
import site.remlit.aster.util.model.fromEntity

internal object NoteRoutes {
	@Serializable
	data class PostNoteBody(
		val cw: String? = null,
		val content: String? = null,
		val visibility: String,
	)

	fun register() =
		RouteRegistry.registerRoute {
			authentication {
				get("/api/note/{id}") {
					val note = NoteService.getById(call.parameters.getOrFail("id"))

					if (
						note == null ||
						!note.user.activated ||
						note.user.suspended ||
						(note.visibility != Visibility.Public &&
								note.visibility != Visibility.Unlisted) ||
						(Configuration.hideRemoteContent && note.user.host != null && call.attributes.getOrNull(
							authenticatedUserKey
						) == null)
					) throw ApiException(HttpStatusCode.NotFound, "Note not found.")

					call.respond(note)
				}

				get("/api/note/{id}/replies") {
					throw ApiException(HttpStatusCode.NotImplemented)
				}
			}

			authentication(
				required = true,
			) {
				delete("/api/note/{id}") {
					val authenticatedUser = call.attributes[authenticatedUserKey]

					val note = NoteService.getById(call.parameters.getOrFail("id")) ?: throw ApiException(
						HttpStatusCode.NotFound,
						"Note not found."
					)

					if (
						note.user.id != authenticatedUser.id.toString() &&
						!RoleService.isModOrAdmin(authenticatedUser.id.toString())
					)
						throw ApiException(HttpStatusCode.Forbidden, "You don't have permission to delete this.")

					NoteService.deleteById(note.id)

					call.respond(HttpStatusCode.OK)
				}

				patch("/api/note/{id}") {
					throw ApiException(HttpStatusCode.NotImplemented)
				}

				post("/api/note") {
					val authenticatedUser = call.attributes[authenticatedUserKey]

					val body = call.receive<PostNoteBody>()

					if (body.content.isNullOrBlank())
						throw ApiException(HttpStatusCode.BadRequest, "Content required")

					val note = NoteService.create(
						IdentifierService.generate(),
						authenticatedUser,
						body.cw,
						body.content,
						Visibility.fromString(body.visibility)
					)

					call.respond(note)
				}

				post("/api/note/{id}/repeat") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val id = call.parameters.getOrFail("id")

					val body = call.receive<PostNoteBody>()

					val repeat = NoteService.repeat(
						User.fromEntity(authenticatedUser),
						id,
						body.cw,
						body.content,
						Visibility.fromString(body.visibility)
					)

					call.respond(repeat)
				}

				post("/api/note/{id}/like") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val id = call.parameters.getOrFail("id")

					NoteService.like(
						User.fromEntity(authenticatedUser),
						id
					)

					call.respond(
						HttpStatusCode.OK,
						NoteService.getById(id) ?: throw ApiException(HttpStatusCode.NotFound, "Note not found")
					)
				}

				post("/api/note/{id}/react") {
					throw ApiException(HttpStatusCode.NotImplemented)
				}

				post("/api/note/{id}/bookmark") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val note = NoteService.getById(call.parameters.getOrFail("id"))

					if (
						note == null ||
						!note.user.activated ||
						note.user.suspended ||
						!(VisibilityService.canISee(
							note.visibility,
							note.user.id,
							note.to,
							authenticatedUser.id.toString()
						))
					) throw ApiException(HttpStatusCode.NotFound, "Note not found.")

					BookmarkService.create(User.fromEntity(authenticatedUser), note.id)

					call.respond(HttpStatusCode.OK)
				}

				post("/api/note/{id}/bite") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val note = NoteService.getById(call.parameters.getOrFail("id"))

					if (
						note == null ||
						!note.user.activated ||
						note.user.suspended ||
						!(VisibilityService.canISee(
							note.visibility,
							note.user.id,
							note.to,
							authenticatedUser.id.toString()
						))
					) throw ApiException(HttpStatusCode.NotFound, "Note not found.")

					if (note.user.id == authenticatedUser.id.toString())
						throw ApiException(HttpStatusCode.BadRequest, "You can't bite your own note")

					NotificationService.bite(UserEntity[note.user.id], authenticatedUser, note)

					call.respond(HttpStatusCode.OK)
				}

				/* Hide post and all replies to it, use conversation to determine replies */
				post("/api/note/{id}/hide") {
					throw ApiException(HttpStatusCode.NotImplemented)
				}

				/* stop receiving notifications for this post, user conversation for this, too */
				post("/api/note/{id}/mute") {
					throw ApiException(HttpStatusCode.NotImplemented)
				}
			}
		}
}
