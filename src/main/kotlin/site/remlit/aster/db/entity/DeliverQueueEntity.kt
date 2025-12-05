package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.DeliverQueueTable

class DeliverQueueEntity(id: EntityID<String>) : Entity<String>(id = id) {
	companion object : EntityClass<String, DeliverQueueEntity>(DeliverQueueTable)

	var status by DeliverQueueTable.status

	var content by DeliverQueueTable.content
	var sender by UserEntity optionalReferencedOn DeliverQueueTable.sender
	var inbox by DeliverQueueTable.inbox

	val createdAt by DeliverQueueTable.createdAt

	var stacktrace by DeliverQueueTable.stacktrace
	var retryAt by DeliverQueueTable.retryAt
	var retries by DeliverQueueTable.retries
}
