package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.common.model.type.PolicyType
import site.remlit.aster.util.TEXT_LONG
import site.remlit.aster.util.TEXT_MEDIUM
import site.remlit.aster.util.TEXT_TINY

object PolicyTable : IdTable<String>("policy") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val type = enumeration("type", PolicyType::class)

	val host = varchar("host", length = TEXT_MEDIUM)
	val content = varchar("content", length = TEXT_LONG)
		.nullable()

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
