package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.UserPrivateTable

class UserPrivateEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, UserPrivateEntity>(UserPrivateTable)

	var totpSecret by UserPrivateTable.totpSecret
	var password by UserPrivateTable.password
	var privateKey by UserPrivateTable.privateKey
}
