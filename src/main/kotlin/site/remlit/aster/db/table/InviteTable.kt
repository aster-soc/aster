package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.util.TEXT_TINY

object InviteTable : IdTable<String>("invite") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val code = varchar("code", length = TEXT_TINY)
		.uniqueIndex()

	val user = optReference("user", UserTable.id, onDelete = ReferenceOption.CASCADE)
	val creator = reference("creator", UserTable.id, onDelete = ReferenceOption.CASCADE)

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val usedAt = datetime("usedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
