package site.remlit.aster.service.ap.inbox

import kotlinx.serialization.json.decodeFromJsonElement
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.ApNote
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.activity.ApCreateActivity
import site.remlit.aster.service.ap.ApNoteService
import site.remlit.aster.util.jsonConfig

class ApCreateHandler : ApInboxHandler {
	private val logger = LoggerFactory.getLogger(ApCreateHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val create = jsonConfig.decodeFromString<ApCreateActivity>(String(job.content.bytes))
		val copy = create.copy()

		when (copy.`object`) {
			is ApIdOrObject.Id -> {
				// todo: ApGenericResolver
				ApNoteService.resolve(copy.`object`.value)
					?: throw IllegalArgumentException("Note ${copy.`object`.value} not found")
			}

			is ApIdOrObject.Object -> {
				val obj = jsonConfig.decodeFromJsonElement<ApTypedObject>(copy.`object`.value)
				when (obj.type) {
					"Note" -> handleNote(jsonConfig.decodeFromJsonElement<ApNote>(copy.`object`.value))
					else -> throw NotImplementedError("No Create handler for ${obj.type}")
				}
			}
		}
	}

	suspend fun handleNote(note: ApNote) {
		ApNoteService.resolve(note.id)
			?: throw IllegalArgumentException("Note ${note.id} not found")
	}
}
