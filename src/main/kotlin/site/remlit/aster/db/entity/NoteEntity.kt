package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.NoteTable

class NoteEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, NoteEntity>(NoteTable)

	var apId by NoteTable.apId
	var conversation by NoteTable.conversation

	var user by UserEntity referencedOn NoteTable.user
	var replyingTo by NoteEntity optionalReferencedOn NoteTable.replyingTo

	var cw by NoteTable.cw
	var content by NoteTable.content

	var visibility by NoteTable.visibility

	var to by NoteTable.to
	var tags by NoteTable.tags
	var emojis by NoteTable.emojis

	var repeat by NoteEntity optionalReferencedOn NoteTable.repeat

	var attachments by NoteTable.attachments

	var createdAt by NoteTable.createdAt
	var updatedAt by NoteTable.updatedAt
}
