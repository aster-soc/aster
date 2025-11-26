package site.remlit.aster.service

import at.favre.lib.crypto.bcrypt.BCrypt
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.PasswordResetCodeEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.entity.UserPrivateEntity
import site.remlit.aster.db.table.PasswordResetCodeTable
import site.remlit.aster.model.Service
import site.remlit.aster.util.PASSWORD_HASH_COST
import site.remlit.aster.util.PASSWORD_MIN_LENGTH

/**
 * Service for managing password resets.
 *
 * @since 2025.11.4.0-SNAPSHOT
 * */
object PasswordResetService : Service {
	private val logger = LoggerFactory.getLogger(PasswordResetService::class.java)

	/**
	 * Creates a code to be used by user resetting password.
	 *
	 * @param user User to have password reset
	 * @param creator Creator of the password reset code
	 *
	 * @return Code to be used to reset user's password
	 * */
	@JvmStatic
	fun createCode(
		user: UserEntity,
		creator: UserEntity,
	): String {
		val code = RandomService.generateString()

		transaction {
			PasswordResetCodeEntity.new(IdentifierService.generate()) {
				this.code = code
				this.user = user
				this.creator = creator
			}
		}

		return code
	}

	/**
	 * Get the entity of the password reset code specified.
	 *
	 * @param code Password reset code string
	 *
	 * @return Password reset code entity
	 * */
	@JvmStatic
	fun getCode(code: String): PasswordResetCodeEntity? = transaction {
		PasswordResetCodeEntity
			.find { PasswordResetCodeTable.code eq code }
			.singleOrNull()
	}

	/**
	 * Mark a password reset code as used and set new password.
	 *
	 * @param code Password reset code string
	 * @param user User resetting password
	 * @param password User's new password
	 * */
	@JvmStatic
	fun resetPassword(
		code: String,
		password: String,
	) {
		val entity = getCode(code)
			?: throw IllegalArgumentException("Code not found")

		if (entity.usedAt != null)
			throw IllegalArgumentException("Code not found")

		val user = transaction { entity.user }

		if (password.length < PASSWORD_MIN_LENGTH)
			throw IllegalArgumentException("Password must be at least $PASSWORD_MIN_LENGTH characters")

		val hashedPassword = BCrypt.withDefaults().hashToString(PASSWORD_HASH_COST, password.toCharArray())

		transaction {
			UserPrivateEntity.findByIdAndUpdate(user.id.toString()) {
				it.password = hashedPassword
			}
			PasswordResetCodeEntity.findByIdAndUpdate(entity.id.toString()) {
				it.usedAt = TimeService.now()
			}
		}
	}
}
