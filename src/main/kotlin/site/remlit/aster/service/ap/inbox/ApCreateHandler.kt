package site.remlit.aster.service.ap.inbox

import kotlinx.serialization.json.decodeFromJsonElement
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.ApNote
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.activity.ApCreateActivity
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.UserService
import site.remlit.aster.service.ap.ApNoteService
import site.remlit.aster.util.jsonConfig

class ApCreateHandler : ApInboxHandler {
	private val logger = LoggerFactory.getLogger(ApCreateHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val create = jsonConfig.decodeFromString<ApCreateActivity>(String(job.content.bytes))
		val copy = create.copy()

		val creator = UserService.getByApId(create.actor ?: return) ?: return
		val firstFollowerId = RelationshipService.getFollowers(creator).firstOrNull()?.id

		when (copy.`object`) {
			is ApIdOrObject.Id -> {
				// todo: ApGenericResolver
				ApNoteService.resolve(copy.`object`.value, user = firstFollowerId)
					?: throw IllegalArgumentException("Note ${copy.`object`.value} not found")
			}

			is ApIdOrObject.Object -> {
				val obj = jsonConfig.decodeFromJsonElement<ApTypedObject>(copy.`object`.value)
				when (obj.type) {
					"Note" -> handleNote(jsonConfig.decodeFromJsonElement<ApNote>(copy.`object`.value), firstFollowerId)
					else -> throw NotImplementedError("No Create handler for ${obj.type}")
				}
			}
		}
	}

	private suspend fun handleNote(
		note: ApNote,
		resolveAs: String?
	) {
		ApNoteService.resolve(note.id, user = resolveAs)
			?: throw IllegalArgumentException("Note ${note.id} not found")
	}
}
