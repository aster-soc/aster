package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.InboxQueueTable

class InboxQueueEntity(id: EntityID<String>) : Entity<String>(id = id) {
	companion object : EntityClass<String, InboxQueueEntity>(InboxQueueTable)

	var status by InboxQueueTable.status

	var content by InboxQueueTable.content
	var sender by UserEntity optionalReferencedOn InboxQueueTable.sender

	val createdAt by InboxQueueTable.createdAt

	var stacktrace by InboxQueueTable.stacktrace
	var retryAt by InboxQueueTable.retryAt
	var retries by InboxQueueTable.retries
}
