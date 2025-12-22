package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.util.TEXT_LONG
import site.remlit.aster.util.TEXT_MEDIUM
import site.remlit.aster.util.TEXT_SMALL
import site.remlit.aster.util.TEXT_TINY

object EmojiTable : IdTable<String>("emoji") {
	override val id: Column<EntityID<String>> = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val apId = varchar("apId", length = TEXT_MEDIUM)
		.uniqueIndex()

	val name = varchar("name", length = TEXT_SMALL)
	val category = varchar("category", length = TEXT_SMALL)
		.nullable()
	val host = varchar("host", length = TEXT_MEDIUM)
		.nullable()
	val src = varchar("src", length = TEXT_LONG)

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
