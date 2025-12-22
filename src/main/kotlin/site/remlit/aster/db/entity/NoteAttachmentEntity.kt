package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.NoteAttachmentTable
import site.remlit.aster.db.table.NoteTable

class NoteAttachmentEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, NoteAttachmentEntity>(NoteAttachmentTable)

	var note by NoteEntity referencedOn NoteTable.id

	var src by NoteAttachmentTable.src
	var alt by NoteAttachmentTable.alt
}
