package site.remlit.aster.util.model

import site.remlit.aster.common.model.SmallUser
import site.remlit.aster.common.model.User
import site.remlit.aster.common.util.toLocalInstant
import site.remlit.aster.db.entity.UserEntity


fun User.Companion.fromEntity(entity: UserEntity) = User(
	id = entity.id.toString(),
	apId = entity.apId,
	inbox = entity.inbox,
	outbox = entity.outbox,

	username = entity.username,
	host = entity.host,
	displayName = entity.displayName,
	bio = entity.bio,
	location = entity.location,
	birthday = entity.birthday,

	avatar = entity.avatar,
	avatarAlt = entity.avatarAlt,
	avatarBlurHash = entity.avatarBlurHash,

	banner = entity.banner,
	bannerAlt = entity.bannerAlt,
	bannerBlurHash = entity.bannerBlurHash,

	locked = entity.locked,
	suspended = entity.suspended,
	activated = entity.activated,
	automated = entity.automated,
	discoverable = entity.discoverable,
	indexable = entity.indexable,
	sensitive = entity.sensitive,

	isCat = entity.isCat,
	speakAsCat = entity.speakAsCat,

	createdAt = entity.createdAt.toLocalInstant(),
	updatedAt = entity.updatedAt?.toLocalInstant(),

	publicKey = entity.publicKey
)

fun User.Companion.fromEntities(entities: List<UserEntity>): List<User> =
	entities.map { fromEntity(it) }

/**
 * Convert a user to a small user.
 * */
fun User.toSmall(): SmallUser = SmallUser.fromUser(this)

/**
 * Convert a list of users to a list of small users.
 * */
fun List<User>.toSmall(): List<SmallUser> = this.map { it.toSmall() }
