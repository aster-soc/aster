package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable
import site.remlit.aster.util.TEXT_SMALLER
import site.remlit.aster.util.TEXT_TINY

object TakenUsernameTable : IdTable<String>("taken_username") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val username = varchar("username", length = TEXT_SMALLER)
		.uniqueIndex()

	override val primaryKey = PrimaryKey(id)
}
