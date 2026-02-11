package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.entity.UserPrivateEntity
import site.remlit.aster.db.table.UserPrivateTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.event.user.UserEditEvent
import site.remlit.aster.exception.SetupException
import site.remlit.aster.model.config.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.model.ap.ApActor
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.activity.ApUpdateActivity
import site.remlit.aster.service.ap.ApDeliverService
import site.remlit.aster.service.ap.ApIdService
import site.remlit.aster.service.ap.ApVisibilityService.AS_PUBLIC
import site.remlit.aster.util.model.fromEntity

/**
 * Service for managing users.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object UserService : Service {
	/**
	 * Get a user
	 *
	 * @param where Query to find user
	 *
	 * @return Found user, if any
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): UserEntity? = transaction {
		UserEntity
			.find { where }
			.singleOrNull()
	}

	/**
	 * Get a user's private information.
	 *
	 * @param where Query to find user private
	 *
	 * @return Found user private, if any
	 * */
	@JvmStatic
	fun getPrivate(where: Op<Boolean>): UserPrivateEntity? = transaction {
		UserPrivateEntity
			.find { where }
			.singleOrNull()
	}

	/**
	 * Get user by ID.
	 *
	 * @param id ID of user
	 *
	 * @return Found user, if any
	 * */
	@JvmStatic
	fun getById(id: String): UserEntity? = get(UserTable.id eq id)

	/**
	 * Get user by ActivityPub ID.
	 *
	 * @param apId ActivityPub ID of user
	 *
	 * @return Found user, if any
	 * */
	@JvmStatic
	fun getByApId(apId: String): UserEntity? = get(UserTable.apId eq apId)

	/**
	 * Get user by username.
	 *
	 * @param username Username of user
	 *
	 * @return Found user, if any
	 * */
	@JvmStatic
	fun getByUsername(username: String): UserEntity? = get(UserTable.username eq username and (UserTable.host eq null))

	/**
	 * Get instance actor.
	 *
	 * @return Instance actor user
	 * */
	@JvmStatic
	fun getInstanceActor(): UserEntity {
		val user = getByUsername("instance.actor")
			?: throw SetupException("Instance actor can't be null")

		return user
	}

	/**
	 * Get a user's private information by ID.
	 *
	 * @param id ID of user
	 *
	 * @return Found user private, if any
	 * */
	@JvmStatic
	fun getPrivateById(id: String): UserPrivateEntity? = getPrivate(UserPrivateTable.id eq id)

	/**
	 * Get users
	 *
	 * @param where Query to find users
	 * @param take Number of users to take
	 * @param offset Offset for query
	 *
	 * @return Found users, if any
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<UserEntity> = transaction {
		UserEntity
			.find { where }
			.offset(offset)
			.sortedByDescending { it.createdAt }
			.take(take)
			.toList()
	}

	/**
	 * Count users
	 *
	 * @param where Query to find users
	 *
	 * @return Count of users where query applies
	 * */
	@JvmStatic
	fun count(where: Op<Boolean>): Long = transaction {
		UserEntity
			.find { where }
			.count()
	}

    /**
     * Edit a user.
     *
     * A null value will leave the value unchanged, but an empty value will set it null.
     *
     * @param user User to edit
     * @param displayName Updated display name
     * @param bio Updated bio
     * @param location Updated location
     * @param birthday Updated birthday
     *
     * @param avatar Updated avatar
     * @param avatarAlt Updated avatar alt text
     * @param banner Updated banner
     * @param bannerAlt Updated banner alt text
     *
     * @param locked Updated locked boolean
     * @param automated Updated automated boolean
     * @param discoverable Updated discoverable boolean
     * @param indexable Updated indexable boolean
     * @param sensitive Updated sensitive boolean
     *
     * @param isCat Updated isCat boolean
     * @param speakAsCat Updated speakAsCat boolean
     *
     * @return Updated user
     * */
    @JvmStatic
    fun update(
        user: UserEntity,

        displayName: String? = user.displayName,
        bio: String? = user.bio,
        location: String? = user.location,
        birthday: String? = user.birthday,

        avatar: String? = user.avatar,
        avatarAlt: String? = user.avatarAlt,

        banner: String? = user.banner,
        bannerAlt: String? = user.bannerAlt,

        locked: Boolean = user.locked,
        automated: Boolean = user.automated,
        discoverable: Boolean = user.discoverable,
        indexable: Boolean = user.indexable,
        sensitive: Boolean = user.sensitive,

        isCat: Boolean = user.isCat,
        speakAsCat: Boolean = user.speakAsCat,
    ): UserEntity = transaction {
        UserEntity.findByIdAndUpdate(user.id.toString()) {
            it.displayName = displayName?.ifEmpty { null }
            it.bio = bio?.ifEmpty { null }
            it.location = location?.ifEmpty { null }
            it.birthday = birthday?.ifEmpty { null }

            it.avatar = avatar?.ifEmpty { null }
            it.avatarAlt = avatarAlt?.ifEmpty { null }
			it.avatarBlurHash = if (it.avatar != null)
				DriveService.getBySrc(it.avatar!!)?.blurHash
			else null

            it.banner = banner?.ifEmpty { null }
            it.bannerAlt = bannerAlt?.ifEmpty { null }
			it.bannerBlurHash = if (it.banner != null)
				DriveService.getBySrc(it.banner!!)?.blurHash
			else null

            it.locked = locked
            it.automated = automated
            it.discoverable = discoverable
            it.indexable = indexable
            it.sensitive = sensitive

            it.isCat = isCat
            it.speakAsCat = speakAsCat
        }

        val newUser = getById(user.id.toString())!!

        UserEditEvent(User.fromEntity(newUser)).call()

        if (newUser.isLocal())
            ApDeliverService.deliverToFollowers<ApUpdateActivity>(
                ApUpdateActivity(
                    ApIdService.renderActivityApId(IdentifierService.generate()),
					actor = newUser.apId,
                    `object` = ApIdOrObject.createObject { ApActor.fromEntity(newUser) },
					to = listOf(AS_PUBLIC),
					cc = emptyList()
                ),
				newUser
            )

        return@transaction newUser
    }

	/**
	 * Delete user
	 *
	 * @param where Query to find user
	 * */
	@JvmStatic
	fun delete(where: Op<Boolean>) = get(where)?.delete()
}
