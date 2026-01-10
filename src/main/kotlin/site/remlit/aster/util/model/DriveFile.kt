package site.remlit.aster.util.model

import site.remlit.aster.common.model.DriveFile
import site.remlit.aster.common.model.User
import site.remlit.aster.common.util.toLocalInstant
import site.remlit.aster.db.entity.DriveFileEntity

fun DriveFile.Companion.fromEntity(entity: DriveFileEntity): DriveFile = DriveFile(
	id = entity.id.toString(),
	type = entity.type,
	src = entity.src,
	alt = entity.alt,
	blurHash = entity.blurHash,
	sensitive = entity.sensitive,
	user = User.fromEntity(entity.user),
	createdAt = entity.createdAt.toLocalInstant(),
	updatedAt = entity.updatedAt?.toLocalInstant()
)

fun DriveFile.Companion.fromEntities(entities: List<DriveFileEntity>): List<DriveFile> =
	entities.map { fromEntity(it) }
