package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.PasswordResetCodeTable

class PasswordResetCodeEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, PasswordResetCodeEntity>(PasswordResetCodeTable)

	var code by PasswordResetCodeTable.code

	var user by UserEntity optionalReferencedOn PasswordResetCodeTable.user
	var creator by UserEntity referencedOn PasswordResetCodeTable.creator

	var createdAt by PasswordResetCodeTable.createdAt
	var usedAt by PasswordResetCodeTable.usedAt

}
