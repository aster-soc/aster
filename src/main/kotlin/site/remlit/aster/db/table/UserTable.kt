package site.remlit.aster.db.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import site.remlit.aster.util.TEXT_LONG
import site.remlit.aster.util.TEXT_MEDIUM
import site.remlit.aster.util.TEXT_SMALL
import site.remlit.aster.util.TEXT_SMALLER
import site.remlit.aster.util.TEXT_TINY

object UserTable : IdTable<String>("user") {
	override val id: Column<EntityID<String>> = varchar("id", length = TEXT_TINY)
		.uniqueIndex().entityId()

	val apId = varchar("apId", length = TEXT_MEDIUM)
		.uniqueIndex()
	val inbox = varchar("inbox", length = TEXT_MEDIUM)
	val outbox = varchar("outbox", length = TEXT_MEDIUM)
		.nullable()

	val username = varchar("username", length = TEXT_SMALLER)
	val host = varchar("host", length = TEXT_MEDIUM)
		.nullable()
	val displayName = varchar("displayName", length = TEXT_SMALL)
		.nullable()
	val bio = varchar("bio", length = TEXT_LONG)
		.nullable()
	val location = varchar("location", length = TEXT_SMALL)
		.nullable()
	val birthday = varchar("birthday", length = TEXT_SMALL)
		.nullable()

	val avatar = varchar("avatar", length = TEXT_MEDIUM)
		.nullable()
	val avatarAlt = varchar("avatarAlt", length = TEXT_LONG)
		.nullable()
	val banner = varchar("banner", length = TEXT_MEDIUM)
		.nullable()
	val bannerAlt = varchar("bannerAlt", length = TEXT_LONG)
		.nullable()

	val locked = bool("locked")
		.default(false)
	val suspended = bool("suspended")
		.default(false)
	val activated = bool("activated")
		.default(false)
	val automated = bool("automated")
		.default(false)
	val discoverable = bool("discoverable")
		.default(false)
	val indexable = bool("indexable")
		.default(false)
	val sensitive = bool("sensitive")
		.default(false)

	val roles = array<String>("roles")
		.default(listOf())
	val emojis = array<String>("emojis")
		.default(listOf())

	val isCat = bool("isCat")
		.default(false)
	val speakAsCat = bool("speakAsCat")
		.default(false)

	val followingUrl = varchar("followingUrl", length = TEXT_MEDIUM)
		.uniqueIndex().nullable()
	val followersUrl = varchar("followersUrl", length = TEXT_MEDIUM)
		.uniqueIndex().nullable()

	val createdAt = datetime("createdAt")
		.defaultExpression(CurrentDateTime)
	val updatedAt = datetime("updatedAt")
		.nullable()

	val publicKey = varchar("publicKey", length = TEXT_LONG)

	override val primaryKey = PrimaryKey(id)
}
