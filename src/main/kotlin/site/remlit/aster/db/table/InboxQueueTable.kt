package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.model.QueueStatus

object InboxQueueTable : IdTable<String>("inbox_queue") {
	override val id = varchar("id", length = 125).uniqueIndex("unique_inbox_queue_id").entityId()

	val status = enumeration<QueueStatus>("status")

	val content = blob("content")
	val sender = optReference("sender", UserTable.id, ReferenceOption.CASCADE)

	val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)

	val stacktrace = text("stacktrace").nullable()
	val retryAt = datetime("retryAt").nullable()
	val retries = integer("retries").default(0)

	override val primaryKey = PrimaryKey(id)
}
