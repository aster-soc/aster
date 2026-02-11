package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.Notification
import site.remlit.aster.common.model.Relationship
import site.remlit.aster.common.model.type.NotificationType
import site.remlit.aster.db.entity.NoteEntity
import site.remlit.aster.db.entity.NotificationEntity
import site.remlit.aster.db.entity.RelationshipEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.NoteTable
import site.remlit.aster.db.table.NotificationTable
import site.remlit.aster.db.table.RelationshipTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.event.notification.NotificationCreateEvent
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.util.model.fromEntities
import site.remlit.aster.util.model.fromEntity

/**
 * Service for managing notifications.
 *
 * @since 2025.10.5.0-SNAPSHOT
 * */
object NotificationService : Service {
	/**
	 * Get a notification
	 *
	 * @param where Query to find notification
	 *
	 * @return Notification, if any
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): Notification? = transaction {
		val entity = NotificationEntity
			.find { where }
			.singleOrNull()
			?.load(
				NotificationEntity::to,
				NotificationEntity::from,
				NotificationEntity::note,
				NotificationEntity::relationship
			)

		if (entity != null)
			Notification.fromEntity(entity)
		else null
	}

	/**
	 * Get a notification by ID
	 *
	 * @param id ID of notification
	 *
	 * @return Notification, if any
	 * */
	@JvmStatic
	fun getById(id: String): Notification? = get(NotificationTable.id eq id)

	/**
	 * Reference the "to" user on a notification.
	 * For usage in queries.
	 * */
	@JvmStatic
	val userToAlias = UserTable.alias("to")

	/**
	 * Reference the "from" user on a notification.
	 * For usage in queries.
	 * */
	@JvmStatic
	val userFromAlias = UserTable.alias("from")

	/**
	 * Get many notifications
	 *
	 * @param where Query to find notifications
	 * @param take Number of notifications to take
	 * @param offset Offset for query
	 *
	 * @return Notifications, if any
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<Notification> = transaction {
		val entities = NotificationTable
			.join(userToAlias, JoinType.INNER, NotificationTable.to, userToAlias[UserTable.id])
			.join(userFromAlias, JoinType.INNER, NotificationTable.from, userFromAlias[UserTable.id])
			.join(NoteTable, JoinType.LEFT, NotificationTable.note, NoteTable.id)
			.join(RelationshipTable, JoinType.LEFT, NotificationTable.relationship, RelationshipTable.id)
			.selectAll()
			.where { where }
			.offset(offset)
			.let { NotificationEntity.wrapRows(it) }
			.sortedByDescending { it.createdAt }
			.take(take)
			.toList()

		if (!entities.isEmpty())
			Notification.fromEntities(entities)
		else listOf()
	}

	/**
	 * Create a notification.
	 *
	 * @param type Type of notification
	 * @param to Recipient of notification
	 * @param from Sender of notification
	 * @param note Note related to notification
	 * @param relationship Relationship related to notification
	 *
	 * @return Created notification
	 * */
	@JvmStatic
	fun create(
		type: NotificationType,
		to: UserEntity,
		from: UserEntity,
		note: NoteEntity?,
		relationship: RelationshipEntity?
	): Notification? {
		val id = IdentifierService.generate()
		transaction {
			NotificationEntity.new(id) {
				this.type = type
				this.to = to
				this.from = from
				if (note != null)
					this.note = note
				if (relationship != null)
					this.relationship = relationship
			}
		}

        val notification = getById(id)
        if (notification != null) NotificationCreateEvent(notification).call()

		return notification
	}

	/**
	 * Create a notification.
	 *
	 * @param type Type of notification
	 * @param to Recipient of notification
	 * @param from Sender of notification
	 * @param note Note related to notification
	 * @param relationship Relationship related to notification
	 *
	 * @return Created notification
	 * */
	@JvmStatic
	fun create(
		type: NotificationType,
		to: UserEntity,
		from: UserEntity,
		note: Note?,
		relationship: RelationshipEntity?
	) = create(
		type,
		to,
		from,
		if (note != null) transaction { NoteEntity[note.id] } else null,
		relationship
	)

	/**
	 * Create a notification.
	 *
	 * @param type Type of notification
	 * @param to Recipient of notification
	 * @param from Sender of notification
	 * @param note Note related to notification
	 * @param relationship Relationship related to notification
	 *
	 * @return Created notification
	 * */
	@JvmStatic
	fun create(
		type: NotificationType,
		to: UserEntity,
		from: UserEntity,
		note: NoteEntity?,
		relationship: Relationship?
	) = create(
		type,
		to,
		from,
		note,
		if (relationship != null) transaction { RelationshipEntity[relationship.id] } else null
	)

	/**
	 * Create a notification.
	 *
	 * @param type Type of notification
	 * @param to Recipient of notification
	 * @param from Sender of notification
	 * @param note Note related to notification
	 * @param relationship Relationship related to notification
	 *
	 * @return Created notification
	 * */
	@JvmStatic
	fun create(
		type: NotificationType,
		to: UserEntity,
		from: UserEntity,
		note: Note?,
		relationship: Relationship?
	) = create(
		type,
		to,
		from,
		if (note != null) transaction { NoteEntity[note.id] } else null,
		if (relationship != null) transaction { RelationshipEntity[relationship.id] } else null
	)

	/**
	 * Create a notification.
	 *
	 * @param type Type of notification
	 * @param to Recipient of notification
	 * @param from Sender of notification
	 * @param note Note related to notification
	 *
	 * @return Created notification
	 * */
	@JvmStatic
	fun create(
		type: NotificationType,
		to: UserEntity,
		from: UserEntity,
		note: Note?,
	) = create(
		type,
		to,
		from,
		if (note != null) transaction { NoteEntity[note.id] } else null,
		null as RelationshipEntity?
	)

	/**
	 * Create a notification.
	 *
	 * @param type Type of notification
	 * @param to Recipient of notification
	 * @param from Sender of notification
	 * @param relationship Relationship related to notification
	 *
	 * @return Created notification
	 * */
	@JvmStatic
	fun create(
		type: NotificationType,
		to: UserEntity,
		from: UserEntity,
		relationship: Relationship?,
	) = create(
		type,
		to,
		from,
		null as Note?,
		relationship
	)

	/**
	 * Create a notification.
	 *
	 * @param type Type of notification
	 * @param to Recipient of notification
	 * @param from Sender of notification
	 *
	 * @return Created notification
	 * */
	@JvmStatic
	fun create(
		type: NotificationType,
		to: UserEntity,
		from: UserEntity
	) = create(
		type,
		to,
		from,
		null as NoteEntity?,
		null as RelationshipEntity?
	)

	/**
	 * Create a bite notification.
	 *
	 * @param to Recipient of notification
	 * @param from Sender of notification
	 *
	 * @return Created bite notification
	 * */
	@JvmStatic
	fun bite(
		to: UserEntity,
		from: UserEntity,
		note: Note? = null,
	) = create(NotificationType.Bite, to, from, note)

	/**
	 * Delete a notification
	 *
	 * @param where Query to find notification
	 * */
	@JvmStatic
	fun delete(where: Op<Boolean>) = transaction {
		val entity = NotificationEntity
			.find { where }
			.singleOrNull()
			?.load(
				NotificationEntity::to,
				NotificationEntity::from,
				NotificationEntity::note,
				NotificationEntity::relationship
			)
		if (entity == null) return@transaction

		// notif delete event
		entity.delete()
	}

}
