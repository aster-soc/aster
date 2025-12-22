package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.util.TEXT_MEDIUM
import site.remlit.aster.util.TEXT_SMALL
import site.remlit.aster.util.TEXT_TINY

object RoleTable : IdTable<String>("role") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val type = enumeration("type", RoleType::class)

	val name = varchar("name", length = TEXT_SMALL)
		.uniqueIndex()
	val description = varchar("description", length = TEXT_MEDIUM)
		.nullable()

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
