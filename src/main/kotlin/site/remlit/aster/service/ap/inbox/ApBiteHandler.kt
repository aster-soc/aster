package site.remlit.aster.service.ap.inbox

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.exception.GracefulInboxException
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.activity.ApBiteActivity
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.NotificationService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.UserService
import site.remlit.aster.service.ap.ApActorService
import site.remlit.aster.util.jsonConfig

class ApBiteHandler : ApInboxHandler {
	override suspend fun handle(job: InboxQueueEntity) {
		val activity = jsonConfig.decodeFromString<ApBiteActivity>(String(job.content.bytes))
		val sender = transaction { job.sender }

		if (sender == null) throw GracefulInboxException("Sender not specified")
		if (sender.apId != activity.actor) throw GracefulInboxException("Sender doesn't match activity's actor")

		val targetNote = NoteService.getByApId(activity.target)
		val targetUser = UserService.getByApId(activity.target)

		val realTargetUser = UserService.getById(targetNote?.user?.id ?: targetUser?.id.toString())
			?: throw GracefulInboxException("User not found")

		if (!realTargetUser.isLocal() || !realTargetUser.activated || realTargetUser.suspended)
			throw GracefulInboxException("User not found")

		if (RelationshipService.eitherBlocking(
				sender.id.toString(),
				realTargetUser.id.toString(),
			)
		) throw GracefulInboxException("Relationship prohibits this action")

		NotificationService.bite(
			realTargetUser,
			sender,
			targetNote
		)
	}
}
