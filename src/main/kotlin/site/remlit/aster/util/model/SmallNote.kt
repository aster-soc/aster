package site.remlit.aster.util.model

import site.remlit.aster.common.model.SmallNote
import site.remlit.aster.common.model.SmallUser
import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.NoteEntity
import site.remlit.aster.util.toLocalInstant

fun SmallNote.Companion.fromEntity(entity: NoteEntity): SmallNote {
	return SmallNote(
		id = entity.id.toString(),
		apId = entity.apId,
		user = SmallUser.fromUser(User.fromEntity(entity.user)),
		cw = entity.cw,
		content = entity.content,
		visibility = entity.visibility,
		to = entity.to,
		tags = entity.tags,
		createdAt = entity.createdAt.toLocalInstant(),
		updatedAt = entity.updatedAt?.toLocalInstant(),
	)
}

fun SmallNote.Companion.fromEntities(entities: List<NoteEntity>): List<SmallNote> =
	entities.map { fromEntity(it) }
