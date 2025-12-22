package site.remlit.aster.util.model

import site.remlit.aster.common.model.Relationship
import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.RelationshipEntity
import site.remlit.aster.util.toLocalInstant


fun Relationship.Companion.fromEntity(entity: RelationshipEntity) = Relationship(
	id = entity.id.toString(),

	type = entity.type,

	to = User.fromEntity(entity.to),
	from = User.fromEntity(entity.from),

	pending = entity.pending,

	createdAt = entity.createdAt.toLocalInstant(),
	updatedAt = entity.updatedAt?.toLocalInstant(),
)

fun Relationship.Companion.fromEntities(entities: List<RelationshipEntity>): List<Relationship> =
	entities.map { fromEntity(it) }
