package site.remlit.aster.util.model

import site.remlit.aster.common.model.Instance
import site.remlit.aster.common.util.toLocalInstant
import site.remlit.aster.db.entity.InstanceEntity

fun Instance.Companion.fromEntity(entity: InstanceEntity): Instance = Instance(
	id = entity.id.toString(),
	host = entity.host,
	name = entity.name,
	description = entity.description,
	color = entity.color,
	icon = entity.icon,
	software = entity.software,
	version = entity.version,
	contact = entity.contact,
	createdAt = entity.createdAt.toLocalInstant(),
	updatedAt = entity.updatedAt?.toLocalInstant(),
)

fun Instance.Companion.fromEntities(entities: List<InstanceEntity>): List<Instance> =
	entities.map { fromEntity(it) }
