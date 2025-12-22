package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.common.model.type.RelationshipType
import site.remlit.aster.util.TEXT_LONG
import site.remlit.aster.util.TEXT_TINY

object RelationshipTable : IdTable<String>("relationship") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val type = enumeration("type", RelationshipType::class)

	val to = reference("to", UserTable.id, onDelete = ReferenceOption.CASCADE)
	val from = reference("from", UserTable.id, onDelete = ReferenceOption.CASCADE)

	val pending = bool("pending")
		.default(false)
	val activityId = varchar("activityId", length = TEXT_LONG)
		.nullable()

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
