package site.remlit.aster.service.ap.inbox

import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.exception.GracefulInboxException
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
		val activity = jsonConfig.decodeFromString<ApCreateActivity>(String(job.content.bytes))
		val sender = transaction { job.sender }

		if (sender == null) throw GracefulInboxException("Sender not specified")
		if (sender.apId != activity.actor) throw GracefulInboxException("Sender doesn't match activity's actor")

		val firstFollowerId = RelationshipService.getFollowers(sender)
			.firstOrNull()?.id

		val copy = activity.copy()
		when (copy.`object`) {
			is ApIdOrObject.Id -> ApNoteService.resolve(copy.`object`.value, user = firstFollowerId)
				?: throw GracefulInboxException("Note not found")

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
			?: throw GracefulInboxException("Note not found")
	}
}
