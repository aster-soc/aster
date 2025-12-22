package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.util.HEX_CODE_LENGTH
import site.remlit.aster.util.TEXT_MEDIUM
import site.remlit.aster.util.TEXT_SMALL
import site.remlit.aster.util.TEXT_TINY

object InstanceTable : IdTable<String>("instance") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val host = varchar("host", length = TEXT_MEDIUM)
		.uniqueIndex()

	val name = varchar("name", length = TEXT_SMALL)
		.nullable()
	val description = varchar("description", length = TEXT_MEDIUM)
		.nullable()
	val color = varchar("color", length = HEX_CODE_LENGTH)
		.default("000000")
	val icon = varchar("icon", length = TEXT_SMALL)
		.nullable()

	val software = varchar("software", length = TEXT_SMALL)
		.nullable()
	val version = varchar("version", length = TEXT_SMALL)
		.nullable()
	val contact = varchar("contact", length = TEXT_SMALL)
		.nullable()

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
