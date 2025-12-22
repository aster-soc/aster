package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable
import site.remlit.aster.util.TEXT_MEDIUM
import site.remlit.aster.util.TEXT_SMALL
import site.remlit.aster.util.TEXT_TINY

object UserPrivateTable : IdTable<String>("user_private") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val password = varchar("password", length = TEXT_SMALL)
	val privateKey = varchar("privateKey", length = TEXT_MEDIUM)

	override val primaryKey = PrimaryKey(id)
}
