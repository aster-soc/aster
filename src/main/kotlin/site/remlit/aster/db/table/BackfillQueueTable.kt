package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.model.BackfillType
import site.remlit.aster.model.QueueStatus
import site.remlit.aster.util.TEXT_LONG
import site.remlit.aster.util.TEXT_TINY

object BackfillQueueTable : IdTable<String>("backfill_queue") {
	override val id = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val status = enumeration<QueueStatus>("status")

	val backfillType = enumeration<BackfillType>("backfillType")
	val target = varchar("target", length = TEXT_LONG)

	val stacktrace = text("stacktrace")
		.nullable()
	val retryAt = datetime("retryAt")
		.nullable()
	val retries = integer("retries")
		.default(0)

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)

	override val primaryKey = PrimaryKey(id)
}

