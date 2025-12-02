package site.remlit.aster.service.ap.inbox

import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.activity.ApBiteActivity
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.NotificationService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.UserService
import site.remlit.aster.service.ap.ApActorService
import site.remlit.aster.util.jsonConfig

class ApBiteHandler : ApInboxHandler() {
	override suspend fun handle(job: InboxQueueEntity) {
		val activity = jsonConfig.decodeFromString<ApBiteActivity>(String(job.content.bytes))

		if (activity.actor == null) return
		val sender = ApActorService.resolve(activity.actor)
			?: throw IllegalArgumentException("Sender could not be resolved")

		val targetNote = NoteService.getByApId(activity.target)
		val targetUser = UserService.getByApId(activity.target)

		val realTargetUser = UserService.getById(targetNote?.user?.id ?: targetUser?.id.toString())
			?: return

		if (realTargetUser.host != null || !realTargetUser.activated || realTargetUser.suspended)
			return

		if (RelationshipService.eitherBlocking(
				sender.id.toString(),
				realTargetUser.id.toString(),
			)
		) return

		NotificationService.bite(
			realTargetUser,
			sender,
			targetNote
		)
	}
}
