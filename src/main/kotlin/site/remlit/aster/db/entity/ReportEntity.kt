package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.ReportTable

class ReportEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, ReportEntity>(ReportTable)

	var sender by UserEntity referencedOn ReportTable.sender

	var user by UserEntity optionalReferencedOn ReportTable.user
	var note by NoteEntity optionalReferencedOn ReportTable.note

	var comment by ReportTable.comment

	var resolvedBy by UserEntity optionalReferencedOn ReportTable.resolvedBy

	val createdAt by ReportTable.createdAt
	var updatedAt by ReportTable.updatedAt
}

