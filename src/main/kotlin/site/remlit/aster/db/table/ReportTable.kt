package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object ReportTable : IdTable<String>("report") {
	override val id = varchar("id", length = 125).uniqueIndex("unique_report_id").entityId()

	val sender = reference("sender", UserTable.id, onDelete = ReferenceOption.CASCADE)

	val user = optReference("user", UserTable.id, onDelete = ReferenceOption.CASCADE)
	val note = optReference("note", NoteTable.id, onDelete = ReferenceOption.CASCADE)

	val comment = varchar("comment", length = 10000).nullable()

	val resolvedBy = optReference("resolvedBy", UserTable.id, onDelete = ReferenceOption.CASCADE)

	val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt").nullable()

	override val primaryKey = PrimaryKey(id)
}
