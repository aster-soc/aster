package site.remlit.aster.service

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.Url
import io.ktor.http.fullPath
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.db.entity.RoleEntity
import site.remlit.aster.db.entity.TakenUsernameEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.entity.UserPrivateEntity
import site.remlit.aster.db.table.RoleTable
import site.remlit.aster.db.table.TakenUsernameTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.KeyType
import site.remlit.aster.model.Service
import site.remlit.aster.service.DriveService.generateBlurHash
import site.remlit.aster.service.ap.ApIdService
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * Service for ensuring the instance is properly set up.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object SetupService : Service {
	private val logger: Logger = LoggerFactory.getLogger(SetupService::class.java)

	/**
	 * Ensure instance is properly set up
	 * */
	@ApiStatus.Internal
	internal fun setup() {
		setupRoles()
		setupInstanceActor()
		setupUsedUsernames()

		val pluginsDir = Path("plugins")
		if (!pluginsDir.exists()) Files.createDirectories(pluginsDir)
	}

	/**
	 * Creates admin and mod roles, if they don't already exist
	 * */
	private fun setupRoles() {
		val existingAdminRole = RoleService.get(RoleTable.type eq RoleType.Admin)

		if (existingAdminRole != null) {
			logger.debug("Admin role already exists")
		} else {
			logger.warn("Admin role missing, generating...")

			val id = IdentifierService.generate()

			transaction {
				RoleEntity.new(id) {
					name = "Admin"
					type = RoleType.Admin
				}
			}

			logger.info("Admin role generated")
		}

		val existingModRole = RoleService.get(RoleTable.type eq RoleType.Mod)

		if (existingModRole != null) {
			logger.debug("Mod role already exists")
		} else {
			logger.warn("Mod role missing, generating...")

			val id = IdentifierService.generate()

			transaction {
				RoleEntity.new(id) {
					name = "Mod"
					type = RoleType.Mod
				}
			}

			logger.info("Mod role generated")
		}
	}


	/**
	 * Creates instance actor, if it doesn't already exist
	 * */
	private fun setupInstanceActor() {
		val existingActor = UserService.get(
			UserTable.username eq "instance.actor"
				and (UserTable.host eq null)
		)

		if (existingActor != null) {
			logger.debug("Instance actor already exists")
		} else {
			logger.warn("Instance actor missing, generating...")

			val securePassword = RandomService.generateString()

			val id = IdentifierService.generate()
			val hashedPassword = BCrypt.withDefaults().hashToString(12, securePassword.toCharArray())

			val keypair = KeypairService.generate()

			transaction {
				UserEntity.new(id) {
					apId = ApIdService.renderUserApId(id)
					inbox = ApIdService.renderInboxApId(id)
					outbox = ApIdService.renderOutboxApId(id)
					username = "instance.actor"
					activated = true
					automated = true
					followingUrl = ApIdService.renderFollowingApId(id)
					followersUrl = ApIdService.renderFollowersApId(id)
					publicKey = KeypairService.keyToPem(KeyType.Public, keypair)
				}
				UserPrivateEntity.new(id) {
					password = hashedPassword
					privateKey = KeypairService.keyToPem(KeyType.Private, keypair)
				}
			}

			logger.info("Instance actor generated")
		}
	}

	private fun setupUsedUsernames() {
		UserService.getMany(UserTable.host eq null).forEach { user ->
			transaction {
				val takenUsername = TakenUsernameEntity.find(TakenUsernameTable.username eq user.username)
					.singleOrNull()

				if (takenUsername == null)
					return@transaction

				logger.debug("Added taken username ${takenUsername.username}")

				TakenUsernameEntity.new(user.id.toString()) {
					username = user.username
				}
			}
		}
	}
}
