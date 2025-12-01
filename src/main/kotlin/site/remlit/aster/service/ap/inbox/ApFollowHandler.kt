package site.remlit.aster.service.ap.inbox

import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.type.RelationshipType
import site.remlit.aster.db.entity.InboxQueueEntity
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

class ApFollowHandler : ApInboxHandler() {
	private val logger = LoggerFactory.getLogger(ApFollowHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val follow = jsonConfig.decodeFromString<ApFollowActivity>(String(job.content.bytes))

		val actor = ApActorService.resolve(follow.actor)
			?: throw IllegalArgumentException("Follow sender cannot be found")

		val obj = when (follow.`object`) {
			is ApIdOrObject.Id -> ApActorService.resolve(follow.`object`.value)
			else -> throw IllegalArgumentException("Follow target must be represented as an ID")
		} ?: throw IllegalArgumentException("Follow target cannot be found")

		if (obj.host != null)
			throw IllegalArgumentException("Follow target must be local")

		if (RelationshipService.eitherBlocking(actor.id.toString(), obj.id.toString()))
			throw IllegalArgumentException("Conflicting existing relationship")

		val existingRelationship =
			RelationshipService.getByIds(obj.id.toString(), actor.id.toString())

		if (existingRelationship != null && existingRelationship.type == RelationshipType.Follow) {
			if (existingRelationship.pending) {
				return
			} else {
				ApDeliverService.deliver<ApAcceptActivity>(
					ApAcceptActivity(
						ApIdService.renderActivityApId(IdentifierService.generate()),
						actor = obj.apId,
						`object` = ApIdOrObject.Id(follow.id)
					),
					obj,
					actor.inbox
				)
				return
			}
		}

		RelationshipService.follow(obj.id.toString(), actor.id.toString(), follow.id)
	}
}
