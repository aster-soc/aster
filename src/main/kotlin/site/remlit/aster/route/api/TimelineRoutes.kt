package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.db.table.NoteBookmarkTable
import site.remlit.aster.db.table.NoteTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.BookmarkService
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.TimelineService
import site.remlit.aster.util.authenticatedUserKey
import site.remlit.aster.util.authentication
import site.remlit.aster.util.sql.arrayContains

internal object TimelineRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
			) {
				get("/api/bookmarks") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())

					val notes = BookmarkService.getMany(
						where = NoteBookmarkTable.user eq authenticatedUser.id and
								(NoteBookmarkTable.createdAt less since),
						take = take
					)

					if (notes.isEmpty())
						return@get call.respond(HttpStatusCode.NoContent)

					call.respond(notes)
				}

				get("/api/timeline/home") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())

					val local = call.request.queryParameters["local"]?.toBoolean() ?: true
					val following = RelationshipService.getFollowing(authenticatedUser).map { it.id }

					var where = (NoteTable.createdAt less since) and (
							(NoteTable.visibility inList listOf(
								Visibility.Public,
								Visibility.Unlisted,
								Visibility.Followers
							) and (UserTable.id inList following))
									or (UserTable.id eq authenticatedUser.id.toString())
									or (NoteTable.to arrayContains listOf(authenticatedUser.id.toString()))
							)

					if (local) where = (where or (NoteTable.visibility inList listOf(Visibility.Public)
							and (UserTable.host eq null)))

					val notes = NoteService.getMany(
						where = where,
						take = take
					)

					if (notes.isEmpty())
						return@get call.respond(HttpStatusCode.NoContent)

					call.respond(notes)
				}
			}

			authentication(
				required = Configuration.timeline.local.authRequired,
			) {
				get("/api/timeline/local") {
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())

					val notes = NoteService.getMany(
						where = NoteTable.visibility inList listOf(Visibility.Public)
								and (UserTable.host eq null)
								and (NoteTable.createdAt less since),
						take = take
					)

					if (notes.isEmpty())
						return@get call.respond(HttpStatusCode.NoContent)

					call.respond(notes)
				}
			}

			authentication(
				required = Configuration.timeline.bubble.authRequired,
			) {
				get("/api/timeline/bubble") {
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())

					val hosts = Configuration.timeline.bubble.hosts

					val notes = NoteService.getMany(
						where = NoteTable.visibility inList listOf(Visibility.Public)
								and (UserTable.host inList hosts or (UserTable.host eq null))
								and (NoteTable.createdAt less since),
						take = take
					)

					if (notes.isEmpty())
						return@get call.respond(HttpStatusCode.NoContent)

					call.respond(notes)
				}
			}

			authentication(
				required = Configuration.timeline.public.authRequired,
			) {
				get("/api/timeline/public") {
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())

					val notes = NoteService.getMany(
						NoteTable.visibility inList listOf(Visibility.Public)
								and (NoteTable.createdAt less since),
						take
					)

					// todo: implement hideRemoteContent, making this essentially local tl 2

					if (notes.isEmpty())
						return@get call.respond(HttpStatusCode.NoContent)

					call.respond(notes)
				}
			}
		}
}
