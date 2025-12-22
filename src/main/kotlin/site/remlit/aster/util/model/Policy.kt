package site.remlit.aster.util.model

import site.remlit.aster.common.model.Policy
import site.remlit.aster.db.entity.PolicyEntity
import site.remlit.aster.util.toLocalInstant


fun Policy.Companion.fromEntity(entity: PolicyEntity): Policy = Policy(
	id = entity.id.toString(),
	type = entity.type,
	host = entity.host,
	content = entity.content,
	createdAt = entity.createdAt.toLocalInstant(),
	updatedAt = entity.updatedAt?.toLocalInstant(),
)

fun Policy.Companion.fromEntities(entities: List<PolicyEntity>): List<Policy> = entities.map { fromEntity(it) }
