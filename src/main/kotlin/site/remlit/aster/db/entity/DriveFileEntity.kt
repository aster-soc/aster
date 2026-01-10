package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.DriveFileTable

class DriveFileEntity(id: EntityID<String>) : Entity<String>(id = id) {
	companion object : EntityClass<String, DriveFileEntity>(DriveFileTable)

	var type by DriveFileTable.type
	var src by DriveFileTable.src
	var alt by DriveFileTable.alt
	var blurHash by DriveFileTable.blurHash

	var sensitive by DriveFileTable.sensitive

	var user by UserEntity referencedOn DriveFileTable.user

	val createdAt by DriveFileTable.createdAt
	var updatedAt by DriveFileTable.updatedAt
}
