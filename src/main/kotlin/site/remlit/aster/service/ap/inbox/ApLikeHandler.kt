package site.remlit.aster.service.ap.inbox

import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.activity.ApLikeActivity
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.ap.ApActorService
import site.remlit.aster.service.ap.ApNoteService
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.model.fromEntity

class ApLikeHandler : ApInboxHandler {
	override suspend fun handle(job: InboxQueueEntity) {
		val activity = jsonConfig.decodeFromString<ApLikeActivity>(String(job.content.bytes))

		if (activity.actor == null) return
		val actor = ApActorService.resolve(activity.actor)
			?: throw IllegalArgumentException("Sender could not be resolved")

		val obj = when (activity.`object`) {
			is ApIdOrObject.Id -> ApNoteService.resolve(activity.`object`.value)
			else -> throw IllegalArgumentException("Target must be represented as an ID")
		} ?: return

		if (RelationshipService.eitherBlocking(actor.id.toString(), obj.user.id))
			throw IllegalArgumentException("Relationship prohibits this action")

		NoteService.like(User.fromEntity(actor), obj.id, false)
	}
}
