package site.remlit.aster.service.ap.inbox

import kotlinx.serialization.json.decodeFromJsonElement
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.InboxHandler
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.activity.ApAcceptActivity
import site.remlit.aster.model.ap.activity.ApFollowActivity
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.util.jsonConfig

class ApAcceptHandler : InboxHandler {
	private val logger = LoggerFactory.getLogger(ApAcceptHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val activity = jsonConfig.decodeFromString<ApAcceptActivity>(String(job.content.bytes))

		val copy = activity.copy()
		when (copy.`object`) {
			is ApIdOrObject.Id -> {
				handleFollow(copy.`object`.value)
			}

			is ApIdOrObject.Object -> {
				val obj = jsonConfig.decodeFromJsonElement<ApTypedObject>(copy.`object`.value)
				when (obj.type) {
					"Follow" -> handleFollow(jsonConfig.decodeFromJsonElement<ApFollowActivity>(copy.`object`.value).id)
					else -> throw NotImplementedError("No Follow handler for ${obj.type}")
				}
			}
		}
	}

	private fun handleFollow(apId: String) {
		RelationshipService.acceptByApId(apId)
	}
}
