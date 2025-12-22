package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.util.TEXT_TINY

object PasswordResetCodeTable : IdTable<String>("password_reset_code") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val code = varchar("code", length = TEXT_TINY)
		.uniqueIndex()

	val user = reference("user", UserTable.id, onDelete = ReferenceOption.CASCADE)
	val creator = reference("creator", UserTable.id, onDelete = ReferenceOption.CASCADE)

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val usedAt = datetime("usedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
