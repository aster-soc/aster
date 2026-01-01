package site.remlit.aster.service.ap.inbox

import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.User
import site.remlit.aster.common.util.extractString
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.NoteTable
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.ApNote
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.activity.ApAnnounceActivity
import site.remlit.aster.model.ap.activity.ApLikeActivity
import site.remlit.aster.model.ap.activity.ApUndoActivity
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.ap.ApNoteService
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.model.fromEntity

class ApUndoHandler : ApInboxHandler {
	private val logger = LoggerFactory.getLogger(ApUndoHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		val undo = jsonConfig.decodeFromString<ApUndoActivity>(String(job.content.bytes))
		val sender = transaction { job.sender }
		val copy = undo.copy()

		if (sender == null)
			throw IllegalArgumentException("Undo Activity must have a sender")

		when (copy.`object`) {
			is ApIdOrObject.Id -> throw IllegalArgumentException("Undo object must not be an ID")
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
		} ?: return

		val repeat = NoteService.get(NoteTable.user eq sender.id and (NoteService.repeatAlias[NoteTable.id] eq repeatedNote.id))
			?: return

		NoteService.deleteById(repeat.id)
	}

	private suspend fun handleLike(
		like: ApLikeActivity,
		sender: UserEntity
	) {
		if (like.`object` is ApIdOrObject.Object)
			throw IllegalArgumentException("Undo Like object must not be an ID")

		val note = ApNoteService.resolve((like.`object` as ApIdOrObject.Id).value)
			?: return

		NoteService.unlike(
			User.fromEntity(sender),
			note.id
		)
	}

	//suspend fun handleFollow(follow: ApFollowActivity, sender: UserEntity) {}
}
