package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.BackfillQueueTable

class BackfillQueueEntity(id: EntityID<String>) : Entity<String>(id = id) {
	companion object : EntityClass<String, BackfillQueueEntity>(BackfillQueueTable)

	var status by BackfillQueueTable.status

	var backfillType by BackfillQueueTable.backfillType
	var target by BackfillQueueTable.target

	var stacktrace by BackfillQueueTable.stacktrace
	var retryAt by BackfillQueueTable.retryAt
	var retries by BackfillQueueTable.retries

	var createdAt by BackfillQueueTable.createdAt
}
