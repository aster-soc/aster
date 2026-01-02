package site.remlit.aster.service.ap.inbox

import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.exception.GracefulInboxException
import site.remlit.aster.model.ap.ApActor
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.ApNote
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.activity.ApUndoActivity
import site.remlit.aster.model.ap.activity.ApUpdateActivity
import site.remlit.aster.service.ap.ApActorService
import site.remlit.aster.service.ap.ApNoteService
import site.remlit.aster.util.jsonConfig

class ApUpdateHandler : ApInboxHandler {
	private val logger = LoggerFactory.getLogger(ApUpdateHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val activity = jsonConfig.decodeFromString<ApUpdateActivity>(String(job.content.bytes))
		val sender = transaction { job.sender }

		if (sender == null) throw GracefulInboxException("Sender not specified")
		if (sender.apId != activity.actor) throw GracefulInboxException("Sender doesn't match activity's actor")

		val copy = activity.copy()
		when (copy.`object`) {
			is ApIdOrObject.Id -> throw GracefulInboxException("Update object must not be an ID")
			is ApIdOrObject.Object -> {
				val typedObject = jsonConfig.decodeFromJsonElement<ApTypedObject>(copy.`object`.value)

				when (typedObject.type) {
					"Person", "Service" -> handleActor(
						jsonConfig.decodeFromJsonElement<ApActor>(copy.`object`.value)
					)

					"Note" -> handleNote(
						jsonConfig.decodeFromJsonElement<ApNote>(copy.`object`.value)
					)
				}
			}
		}
	}

	private suspend fun handleActor(actor: ApActor) {
		ApActorService.resolve(actor.id, true)
	}

	private suspend fun handleNote(note: ApNote) {
		ApNoteService.resolve(note.id, true)
	}
}
