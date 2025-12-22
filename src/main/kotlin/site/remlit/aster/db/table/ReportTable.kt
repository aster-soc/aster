package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.util.TEXT_MEDIUM
import site.remlit.aster.util.TEXT_TINY

object ReportTable : IdTable<String>("report") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val sender = reference("sender", UserTable.id, onDelete = ReferenceOption.CASCADE)

	val user = optReference("user", UserTable.id, onDelete = ReferenceOption.CASCADE)
	val note = optReference("note", NoteTable.id, onDelete = ReferenceOption.CASCADE)

	val comment = varchar("comment", length = TEXT_MEDIUM)
		.nullable()

	val resolvedBy = optReference("resolvedBy", UserTable.id, onDelete = ReferenceOption.CASCADE)

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
