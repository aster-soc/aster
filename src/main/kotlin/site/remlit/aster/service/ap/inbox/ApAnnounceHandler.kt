package site.remlit.aster.service.ap.inbox

import kotlinx.serialization.json.decodeFromJsonElement
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.ApNote
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.activity.ApAnnounceActivity
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.UserService
import site.remlit.aster.service.ap.ApNoteService
import site.remlit.aster.service.ap.ApVisibilityService
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.model.fromEntity

class ApAnnounceHandler : ApInboxHandler {
	private val logger = LoggerFactory.getLogger(ApAnnounceHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val announce = jsonConfig.decodeFromString<ApAnnounceActivity>(String(job.content.bytes))
		val copy = announce.copy()

		val creator = UserService.getByApId(announce.actor ?: return) ?: return
		val firstFollowerId = RelationshipService.getFollowers(creator).firstOrNull()?.id

		val visibility = ApVisibilityService.determineVisibility(
			announce.to,
			announce.cc,
			creator.followersUrl
		)

		when (copy.`object`) {
			is ApIdOrObject.Id -> {
				val resolved = ApNoteService.resolve(copy.`object`.value, user = firstFollowerId)
					?: throw IllegalArgumentException("Note ${copy.`object`.value} not found")

				handleNote(resolved, creator, visibility)
			}

			is ApIdOrObject.Object -> {
				val obj = jsonConfig.decodeFromJsonElement<ApTypedObject>(copy.`object`.value)

				when (obj.type) {
					"Note" -> {
						val note = jsonConfig.decodeFromJsonElement<ApNote>(copy.`object`.value)

						val resolved = ApNoteService.resolve(note.id, user = firstFollowerId)
							?: throw IllegalArgumentException("Note ${copy.`object`.value} not found")

						handleNote(resolved, creator, visibility)
					}

					else -> throw NotImplementedError("No Announce handler for ${obj.type}")
				}
			}
		}
	}

	private fun handleNote(
		note: Note,
		user: UserEntity,
		visibility: Visibility
	) {
		NoteService.repeat(
			User.fromEntity(user),
			note.id,
			visibility = visibility
		)
	}
}

