package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import site.remlit.aster.util.TEXT_LONG
import site.remlit.aster.util.TEXT_TINY

object NoteAttachmentTable : IdTable<String>("note_attachment") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val note = reference("note", NoteTable, onDelete = ReferenceOption.CASCADE)

	val src = text("src")
	val alt = varchar("alt", length = TEXT_LONG)
		.nullable()
}
