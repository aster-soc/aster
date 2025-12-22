package site.remlit.aster.util.model

import site.remlit.aster.common.model.Invite
import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.InviteEntity
import site.remlit.aster.util.toLocalInstant


fun Invite.Companion.fromEntity(entity: InviteEntity): Invite = Invite(
	id = entity.id.toString(),
	code = entity.code,
	user = if (entity.user != null) User.fromEntity(entity.user!!) else null,
	creator = User.fromEntity(entity.creator),
	createdAt = entity.createdAt.toLocalInstant(),
	usedAt = entity.usedAt?.toLocalInstant(),
)

fun Invite.Companion.fromEntities(entities: List<InviteEntity>): List<Invite> =
	entities.map { fromEntity(it) }
