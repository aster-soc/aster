package site.remlit.aster.util.model

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.SmallNote
import site.remlit.aster.common.model.SmallUser
import site.remlit.aster.common.model.User
import site.remlit.aster.common.util.toLocalInstant
import site.remlit.aster.db.entity.NoteEntity
import site.remlit.aster.db.entity.NoteLikeEntity
import site.remlit.aster.db.table.NoteLikeTable
import site.remlit.aster.db.table.NoteTable
import site.remlit.aster.service.NoteService


fun Note.Companion.fromEntity(entity: NoteEntity, depth: Int = 0): Note {
	return Note(
		id = entity.id.toString(),
		apId = entity.apId,
		conversation = entity.conversation,
		user = User.fromEntity(entity.user),
		replyingTo = if (entity.replyingTo != null && depth < 1) Note.fromEntity(
			entity.replyingTo!!,
			1
		) else null,
		cw = entity.cw,
		content = entity.content,
		visibility = entity.visibility,
		to = entity.to,
		tags = entity.tags,
		repeat = if (entity.repeat != null && depth < 2) Note.fromEntity(
			entity.repeat!!,
			1
		) else null,
		createdAt = entity.createdAt.toLocalInstant(),
		updatedAt = entity.updatedAt?.toLocalInstant(),
		likes = findLikes(entity.id.toString()),
		repeats = findRepeats(entity.id.toString())
	)
}

fun Note.Companion.findLikes(id: String): List<SmallUser> {
	val likes = transaction {
		NoteLikeEntity
			.find { NoteLikeTable.note eq id }
			.sortedByDescending { it.createdAt }
			.toList()
	}

	val users = mutableListOf<SmallUser>()

	likes.forEach {
		users.add(
			SmallUser.fromUser(User.fromEntity(it.user))
		)
	}

	return users.toList()
}

fun Note.Companion.findRepeats(id: String): List<SmallNote> =
	NoteService.getManySmall(
		NoteService.repeatAlias[NoteTable.id] eq id
	)

fun Note.Companion.fromEntities(entities: List<NoteEntity>): List<Note> =
	entities.map { fromEntity(it) }
