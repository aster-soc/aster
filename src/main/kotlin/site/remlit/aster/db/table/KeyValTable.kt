package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable
import site.remlit.aster.util.TEXT_TINY

object KeyValTable : IdTable<String>("keyval") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val key = text("key", eagerLoading = true)
		.uniqueIndex()
	val value = text("value", eagerLoading = true)
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
