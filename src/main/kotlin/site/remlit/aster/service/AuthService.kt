package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.db.entity.AuthEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.AuthTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.Service

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

		return generatedToken
	}
}
