package site.remlit.aster.route.ap

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApNote
import site.remlit.aster.model.ap.activity.ApAnnounceActivity
import site.remlit.aster.model.ap.activity.ApCreateActivity
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.ap.ApVisibilityService

internal object ApNoteRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			get("/notes/{id}") {
				call.response.headers.append("Content-Type", "application/activity+json")

				val note = NoteService.getById(call.parameters.getOrFail("id"))

				if (
					note == null ||
					!note.user.activated ||
					note.user.suspended ||
					!note.user.host.isNullOrBlank() ||
					(note.visibility != Visibility.Public &&
						note.visibility != Visibility.Unlisted)
				)
					throw ApiException(HttpStatusCode.NotFound)

				if (note.content == null && note.repeat != null) {
					val (to, cc) = ApVisibilityService.visibilityToCc(
						note.visibility,
						note.user.followersUrl,
						emptyList()
					)

					return@get call.respond(
						ApAnnounceActivity(
							note.apId,
							actor = note.user.apId,
							`object` = ApIdOrObject.Id(note.repeat!!.apId),
							to = to,
							cc = cc
						)
					)
				}

				call.respond(ApNote.fromEntity(note))
			}

			get("/notes/{id}/activity") {
				call.response.headers.append("Content-Type", "application/activity+json")

				val note = NoteService.getById(call.parameters.getOrFail("id"))

				if (note == null || !note.user.activated || note.user.suspended)
					throw ApiException(HttpStatusCode.NotFound)


				if (!note.user.host.isNullOrBlank() ||
					(note.visibility != Visibility.Public &&
						note.visibility != Visibility.Unlisted)
				) throw ApiException(HttpStatusCode.NotFound)

				// todo: these shouldnt be null
				val (to, cc) = ApVisibilityService.visibilityToCc(note.visibility, null, null)

				if (note.content == null && note.repeat != null)
					throw ApiException(HttpStatusCode.NotFound)

				call.respond(
					ApCreateActivity(
						id = note.apId,
						actor = note.user.apId,
						`object` = ApIdOrObject.createObject {
							ApNote.fromEntity(note)
						},
						to = to,
						cc = cc
					)
				)
			}
		}
}
