package site.remlit.aster.service.ap.inbox

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.type.RelationshipType
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.exception.GracefulInboxException
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.activity.ApAcceptActivity
import site.remlit.aster.model.ap.activity.ApFollowActivity
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.ap.ApActorService
import site.remlit.aster.service.ap.ApDeliverService
import site.remlit.aster.service.ap.ApIdService
import site.remlit.aster.util.jsonConfig

class ApFollowHandler : ApInboxHandler {
	private val logger = LoggerFactory.getLogger(ApFollowHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val activity = jsonConfig.decodeFromString<ApFollowActivity>(String(job.content.bytes))
		val sender = transaction { job.sender }

		if (sender == null) throw GracefulInboxException("Sender not specified")
		if (sender.apId != activity.actor) throw GracefulInboxException("Sender doesn't match activity's actor")

		val obj = when (activity.`object`) {
			is ApIdOrObject.Id -> ApActorService.resolve(activity.`object`.value)
			else -> throw GracefulInboxException("Follow object must not be an object")
		} ?: throw GracefulInboxException("Follow object not found")

		if (!obj.isLocal())
			throw GracefulInboxException("Follow object must be local")

		if (RelationshipService.eitherBlocking(sender.id.toString(), obj.id.toString()))
			throw GracefulInboxException("Conflicting existing relationship")

		val existingRelationship =
			RelationshipService.getByIds(obj.id.toString(), sender.id.toString())

		if (existingRelationship != null && existingRelationship.type == RelationshipType.Follow) {
			if (existingRelationship.pending) {
				return
			} else {
				ApDeliverService.deliver<ApAcceptActivity>(
					ApAcceptActivity(
						ApIdService.renderActivityApId(IdentifierService.generate()),
						actor = obj.apId,
						`object` = ApIdOrObject.Id(activity.id)
					),
					obj,
					sender.inbox
				)
				return
			}
		}

		RelationshipService.follow(obj.id.toString(), sender.id.toString(), activity.id)
	}
}
