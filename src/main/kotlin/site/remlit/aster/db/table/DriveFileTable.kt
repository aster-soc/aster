package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.util.TEXT_LONG
import site.remlit.aster.util.TEXT_MEDIUM
import site.remlit.aster.util.TEXT_TINY

object DriveFileTable : IdTable<String>("drive_file") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val type = varchar("type", length = TEXT_TINY)
	val src = varchar("src", length = TEXT_MEDIUM)
	val alt = varchar("alt", length = TEXT_LONG)
		.nullable()
	val blurHash = varchar("blurHash", length = TEXT_TINY)
		.nullable()

	val sensitive = bool("sensitive")
		.default(false)

	val user = reference("user", UserTable.id, onDelete = ReferenceOption.CASCADE)

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
