package site.remlit.aster.service.ap.inbox

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.exception.GracefulInboxException
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.InboxHandler
import site.remlit.aster.model.ap.activity.ApLikeActivity
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.ap.ApNoteService
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.model.fromEntity

class ApLikeHandler : InboxHandler {
	override suspend fun handle(job: InboxQueueEntity) {
		val activity = jsonConfig.decodeFromString<ApLikeActivity>(String(job.content.bytes))
		val sender = transaction { job.sender }

		if (sender == null) throw GracefulInboxException("Sender not specified")
		if (sender.apId != activity.actor) throw GracefulInboxException("Sender doesn't match activity's actor")

		val obj = when (activity.`object`) {
			is ApIdOrObject.Id -> ApNoteService.resolve(activity.`object`.value)
			else -> throw GracefulInboxException("Like object must not be an object")
		} ?: throw GracefulInboxException("Like object not found")

		if (RelationshipService.eitherBlocking(sender.id.toString(), obj.user.id))
			throw GracefulInboxException("Relationship prohibits this action")

		NoteService.like(User.fromEntity(sender), obj.id, false)
	}
}
