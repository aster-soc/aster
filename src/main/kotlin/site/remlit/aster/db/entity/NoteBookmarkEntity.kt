package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.NoteBookmarkTable

class NoteBookmarkEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, NoteBookmarkEntity>(NoteBookmarkTable)

	var user by UserEntity referencedOn NoteBookmarkTable.user
	var note by NoteEntity referencedOn NoteBookmarkTable.note
	var createdAt by NoteBookmarkTable.createdAt
}

