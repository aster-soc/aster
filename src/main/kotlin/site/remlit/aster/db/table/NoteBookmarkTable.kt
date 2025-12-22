package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.util.TEXT_TINY

object NoteBookmarkTable : IdTable<String>("note_bookmark") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val user = reference("user", UserTable.id, onDelete = ReferenceOption.CASCADE)
	val note = reference("note", NoteTable.id, onDelete = ReferenceOption.CASCADE)

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)

	override val primaryKey = PrimaryKey(id)
}
