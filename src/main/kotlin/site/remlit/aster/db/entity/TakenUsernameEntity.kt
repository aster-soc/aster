package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.TakenUsernameTable

class TakenUsernameEntity(id: EntityID<String>) : Entity<String>(id = id) {
	companion object : EntityClass<String, TakenUsernameEntity>(TakenUsernameTable)

	var username by TakenUsernameTable.username
}
