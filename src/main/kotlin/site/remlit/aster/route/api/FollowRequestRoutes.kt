package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import site.remlit.aster.db.table.NoteBookmarkTable
import site.remlit.aster.db.table.RelationshipTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.ApiException
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.BookmarkService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.TimelineService
import site.remlit.aster.util.authenticatedUserKey
import site.remlit.aster.util.authentication

internal object FollowRequestRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true
			) {
				get("/api/follow-requests") {
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())
					val authenticatedUser = call.attributes[authenticatedUserKey]

					val followRequests = RelationshipService.getMany(
						where = RelationshipService.userToAlias[UserTable.id] eq authenticatedUser.id and (
							RelationshipTable.pending eq true
						) and (RelationshipTable.createdAt less since),
						take = take
					)

					if (followRequests.isEmpty())
						return@get call.respond(HttpStatusCode.NoContent)

					call.respond(followRequests)
				}

				get("/api/follow-requests/sent") {
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())
					val authenticatedUser = call.attributes[authenticatedUserKey]

					val followRequests = RelationshipService.getMany(
						where = RelationshipService.userFromAlias[UserTable.id] eq authenticatedUser.id and (
							RelationshipTable.pending eq true
						) and (RelationshipTable.createdAt less since),
						take = take
					)

					if (followRequests.isEmpty())
						return@get call.respond(HttpStatusCode.NoContent)

					call.respond(followRequests)
				}

				post("/api/follow-request/{id}/accept") {
					val id = call.parameters.getOrFail("id")
					val authenticatedUser = call.attributes[authenticatedUserKey]

					val relationship = RelationshipService.getById(id)

					if (relationship == null || !relationship.pending || relationship.to.id != authenticatedUser.id.toString())
						throw ApiException(HttpStatusCode.NotFound, "Relationship not found")

					val new = RelationshipService.accept(id)
						?: throw ApiException(HttpStatusCode.NotFound, "Relationship not found")

					call.respond(new)
				}

				post("/api/follow-request/{id}/reject") {
					val id = call.parameters.getOrFail("id")
					val authenticatedUser = call.attributes[authenticatedUserKey]

					val relationship = RelationshipService.getById(id)

					if (relationship == null || !relationship.pending || relationship.to.id != authenticatedUser.id.toString())
						throw ApiException(HttpStatusCode.NotFound, "Relationship not found")

					RelationshipService.reject(id)

					call.respond(HttpStatusCode.OK)
				}

				post("/api/follow-request/{id}/undo") {
					call.respond(HttpStatusCode.NotImplemented)
				}
			}
		}
}
