package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.util.TEXT_LONG
import site.remlit.aster.util.TEXT_MEDIUM
import site.remlit.aster.util.TEXT_TINY

object NoteTable : IdTable<String>("note") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val apId = varchar("apId", length = TEXT_MEDIUM)
		.uniqueIndex()
	val conversation = varchar("conversation", length = TEXT_MEDIUM)
		.nullable()

	val user = reference("user", UserTable.id, onDelete = ReferenceOption.CASCADE)
	val replyingTo = optReference("replyingTo", NoteTable, onDelete = ReferenceOption.CASCADE)

	val cw = varchar("cw", length = TEXT_MEDIUM)
		.index().nullable()
	val content = varchar("content", length = TEXT_LONG)
		.index().nullable()

	val visibility = enumeration("visibility", Visibility::class)

	val to = array<String>("to")
		.default(listOf())
	val tags = array<String>("tags")
		.default(listOf())
	val emojis = array<String>("emojis")
		.default(listOf())

	val repeat = optReference("repeat", NoteTable, onDelete = ReferenceOption.CASCADE)

	val attachments = array<String>("attachments")
		.default(listOf())

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt")
		.nullable()

	override val primaryKey = PrimaryKey(id)
}
