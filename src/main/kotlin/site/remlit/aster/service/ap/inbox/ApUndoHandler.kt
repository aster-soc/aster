package site.remlit.aster.service.ap.inbox

import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.type.RelationshipType
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.NoteTable
import site.remlit.aster.exception.GracefulInboxException
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.ApNote
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.activity.ApAnnounceActivity
import site.remlit.aster.model.ap.activity.ApFollowActivity
import site.remlit.aster.model.ap.activity.ApLikeActivity
import site.remlit.aster.model.ap.activity.ApUndoActivity
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.ap.ApActorService
import site.remlit.aster.service.ap.ApNoteService
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.model.fromEntity

class ApUndoHandler : ApInboxHandler {
	private val logger = LoggerFactory.getLogger(ApUndoHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val activity = jsonConfig.decodeFromString<ApUndoActivity>(String(job.content.bytes))
		val sender = transaction { job.sender }

		if (sender == null) throw GracefulInboxException("Sender not specified")
		// TODO: Does this need an actor?
		// if (sender.apId != activity.actor) throw GracefulInboxException("Sender doesn't match activity's actor")

		val copy = activity.copy()
		when (copy.`object`) {
			is ApIdOrObject.Id -> throw GracefulInboxException("Undo object must not be an ID")
			is ApIdOrObject.Object -> {
				val obj = jsonConfig.decodeFromJsonElement<ApTypedObject>(copy.`object`.value)
				when (obj.type) {
					"Announce" -> handleAnnounce(
						jsonConfig.decodeFromJsonElement<ApAnnounceActivity>(copy.`object`.value),
						sender
					)

					"Like" -> handleLike(
						jsonConfig.decodeFromJsonElement<ApLikeActivity>(copy.`object`.value),
						sender
					)

					/*
					"Follow" -> handleFollow(
						jsonConfig.decodeFromJsonElement<ApFollowActivity>(copy.`object`.value),
						sender
					)
					*/

					else -> throw NotImplementedError("No Undo handler for ${obj.type}")
				}
			}
		}
	}

	private suspend fun handleAnnounce(
		announce: ApAnnounceActivity,
		sender: UserEntity
	) {
		val repeatedNote = when (announce.`object`) {
			is ApIdOrObject.Id -> ApNoteService.resolve(announce.`object`.value)
			is ApIdOrObject.Object -> ApNoteService.resolve(
				jsonConfig.decodeFromJsonElement<ApNote>(announce.`object`.value).id
			)
		} ?: throw GracefulInboxException("Repeated note not found")

		val repeat = NoteService.get(NoteTable.user eq sender.id and
			(NoteService.repeatAlias[NoteTable.id] eq repeatedNote.id))
				?: throw GracefulInboxException("Repeat note not found")

		NoteService.deleteById(repeat.id)
	}

	private suspend fun handleLike(
		like: ApLikeActivity,
		sender: UserEntity
	) {
		if (like.`object` is ApIdOrObject.Object)
			throw GracefulInboxException("Undo Like object must not be an object")

		val note = ApNoteService.resolve((like.`object` as ApIdOrObject.Id).value)
			?: throw GracefulInboxException("Could not resolve liked note")

		NoteService.unlike(
			User.fromEntity(sender),
			note.id
		)
	}

	suspend fun handleFollow(follow: ApFollowActivity, sender: UserEntity) {
		val target = when (follow.`object`) {
			is ApIdOrObject.Id -> ApActorService.resolve(follow.`object`.value)
			is ApIdOrObject.Object -> throw GracefulInboxException("Undo Follow object must not be an object")
		} ?: throw GracefulInboxException("Follow object not found")

		val relationship = RelationshipService.getByIds(
			RelationshipType.Follow,
			target.id.toString(),
			sender.id.toString()
		) ?: throw GracefulInboxException("Relationship not found")

		RelationshipService.unfollow(relationship.to.id, relationship.from.id)
	}
}
