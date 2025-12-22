package site.remlit.aster.util.model

import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.Notification
import site.remlit.aster.common.model.Relationship
import site.remlit.aster.common.model.User
import site.remlit.aster.common.util.toLocalInstant
import site.remlit.aster.db.entity.NotificationEntity

fun Notification.Companion.fromEntity(entity: NotificationEntity): Notification = Notification(
	id = entity.id.toString(),
	type = entity.type,
	to = User.fromEntity(entity.to),
	from = User.fromEntity(entity.from),
	note = if (entity.note != null) Note.fromEntity(entity.note!!) else null,
	relationship = if (entity.relationship != null) Relationship.fromEntity(entity.relationship!!) else null,
	createdAt = entity.createdAt.toLocalInstant(),
)

fun Notification.Companion.fromEntities(entities: List<NotificationEntity>): List<Notification> =
	entities.map { fromEntity(it) }
