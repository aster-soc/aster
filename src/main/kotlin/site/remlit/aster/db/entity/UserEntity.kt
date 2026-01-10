package site.remlit.aster.db.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import site.remlit.aster.db.table.UserTable

class UserEntity(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, UserEntity>(UserTable)

	var apId by UserTable.apId
	var inbox by UserTable.inbox
	var outbox by UserTable.outbox

	var username by UserTable.username
	var host by UserTable.host
	var displayName by UserTable.displayName
	var bio by UserTable.bio
	var location by UserTable.location
	var birthday by UserTable.birthday

	var avatar by UserTable.avatar
	var avatarAlt by UserTable.avatarAlt
	var avatarBlurHash by UserTable.avatarBlurHash

	var banner by UserTable.banner
	var bannerAlt by UserTable.bannerAlt
	var bannerBlurHash by UserTable.bannerBlurHash

	var locked by UserTable.locked
	var suspended by UserTable.suspended
	var activated by UserTable.activated
	var automated by UserTable.automated
	var discoverable by UserTable.discoverable
	var indexable by UserTable.indexable
	var sensitive by UserTable.sensitive

	var roles by UserTable.roles
	var emojis by UserTable.emojis

	var isCat by UserTable.isCat
	var speakAsCat by UserTable.speakAsCat

	var followingUrl by UserTable.followingUrl
	var followersUrl by UserTable.followersUrl

	var createdAt by UserTable.createdAt
	var updatedAt by UserTable.updatedAt

	var publicKey by UserTable.publicKey

    /**
     * If this user is local or not
     *
     * @since 2025.12.6.0-SNAPSHOT
     * */
    fun isLocal(): Boolean =
        host == null
}
