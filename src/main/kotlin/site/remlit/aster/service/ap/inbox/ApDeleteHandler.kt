package site.remlit.aster.service.ap.inbox

import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.ApTombstone
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.activity.ApDeleteActivity
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.UserService
import site.remlit.aster.util.jsonConfig

class ApDeleteHandler : ApInboxHandler {
	private val logger = LoggerFactory.getLogger(ApDeleteHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val delete = jsonConfig.decodeFromString<ApDeleteActivity>(String(job.content.bytes))
		val copy = delete.copy()

		when (copy.`object`) {
			is ApIdOrObject.Id -> handleAll(copy.`object`.value, transaction { job.sender })

			is ApIdOrObject.Object -> {
				val obj = jsonConfig.decodeFromJsonElement<ApTypedObject>(copy.`object`.value)
				when (obj.type) {
					"Tombstone" -> handleAll(
						jsonConfig.decodeFromJsonElement<ApTombstone>(
							copy.`object`.value
						).id,
						transaction { job.sender }
					)

					else -> throw NotImplementedError("No Delete handler for ${obj.type}")
				}
			}
		}
	}

	private fun handleAll(
		apId: String,
		sender: UserEntity? = null
	) {
		val note = NoteService.getByApId(apId)
		if (note != null && note.user.id == sender?.id.toString()) {
			NoteService.deleteById(note.id)
			logger.debug("Processed delete of note ${note.apId}")
		}

		val user = UserService.getByApId(apId)
		if (user != null) {
			UserService.delete(UserTable.id eq user.id)
			logger.debug("Processed delete of user ${user.apId}")
		}
	}
}
