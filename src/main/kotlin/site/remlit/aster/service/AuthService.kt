package site.remlit.aster.service

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.AuthEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.entity.UserPrivateEntity
import site.remlit.aster.db.table.AuthTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.event.auth.AuthTokenCreateEvent
import site.remlit.aster.event.user.UserTotpRegisterEvent
import site.remlit.aster.event.user.UserTotpUnregisterEvent
import site.remlit.aster.model.Service
import site.remlit.aster.util.model.fromEntity

/**
 * Service for managing user authentication.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object AuthService : Service {
	/**
	 * Get auth entity.
	 *
	 * @param where Query to find auth entity
	 *
	 * @return Auth entity, if it exists
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): AuthEntity? = transaction {
		AuthEntity
			.find { where }
			.singleOrNull()
	}

	/**
	 * Get auth entity by token.
	 *
	 * @param token Token to use to find an auth entity
	 *
	 * @return Auth entity, if it exists
	 * */
	@JvmStatic
	fun getByToken(token: String): AuthEntity? = get(AuthTable.token eq token)

	/**
	 * Creates a new auth token for a user
	 *
	 * @param user ID of a user
	 *
	 * @return Newly created auth token
	 * */
	@JvmStatic
	fun registerToken(user: String): String {
		val id = IdentifierService.generate()
		val generatedToken = RandomService.generateString()

		val user = UserService.get(UserTable.id eq user)!!

		transaction {
			AuthEntity.new(id) {
				token = generatedToken
				this.user = user
			}
		}

		return generatedToken
	}

	/**
	 * Creates a new auth token for a user
	 *
	 * @param user User entity
	 *
	 * @return Newly created auth token
	 * */
	@JvmStatic
	fun registerToken(user: UserEntity): String {
		val id = IdentifierService.generate()
		val generatedToken = RandomService.generateString()

		transaction {
			AuthEntity.new(id) {
				token = generatedToken
				this.user = user
			}
		}

        AuthTokenCreateEvent(getByToken(generatedToken)!!).call()

		return generatedToken
	}

	/**
	 * Register time-based one time passwords for a user
	 *
	 * @param user ID of user
	 *
	 * @return Generated secret
	 * */
	@JvmStatic
	fun registerTotp(user: String): String {
		val user = UserService.getById(user)
			?: throw IllegalArgumentException("User not found")

		val secret = TimeBasedOneTimePasswordUtil.generateBase32Secret()

		transaction {
			UserPrivateEntity.findByIdAndUpdate(user.id.toString()) {
				it.totpSecret = secret
			}
		}

		UserTotpRegisterEvent(User.fromEntity(user)).call()

		return secret
	}

	/**
	 * Determines if a time-based one time password is valid or not
	 *
	 * @param user ID of user
	 * @param code User submitted one time password
	 *
	 * @return If one time password is valid
	 * */
	@JvmStatic
	fun confirmTotp(user: String, code: Int): Boolean {
		val private = UserService.getPrivateById(user)
			?: throw IllegalArgumentException("User not found")

		return TimeBasedOneTimePasswordUtil.generateCurrentNumber(private.totpSecret) == code
	}

	/**
	 * Removes the time-based one time passwords for a user
	 *
	 * @param user ID of user
	 * */
	@JvmStatic
	fun removeTotp(user: String) {
		val user = UserService.getById(user)
			?: throw IllegalArgumentException("User not found")

		transaction {
			UserPrivateEntity.findByIdAndUpdate(user.id.toString()) {
				it.totpSecret = null
			}
		}

		UserTotpUnregisterEvent(User.fromEntity(user)).call()
	}
}
