package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.common.model.type.NotificationType
import site.remlit.aster.util.TEXT_TINY

object NotificationTable : IdTable<String>("notification") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val type = enumeration("type", NotificationType::class)

	val to = reference("to", UserTable.id, onDelete = ReferenceOption.CASCADE)
	val from = reference("from", UserTable.id, onDelete = ReferenceOption.CASCADE)

	val note = optReference("note", NoteTable.id, onDelete = ReferenceOption.CASCADE)
	val relationship = optReference("relationship", RelationshipTable.id, onDelete = ReferenceOption.CASCADE)

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)

	override val primaryKey = PrimaryKey(id)
}

