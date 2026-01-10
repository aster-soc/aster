package site.remlit.aster.service

import io.ktor.http.Url
import io.ktor.http.fullPath
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.common.util.ifFails
import site.remlit.aster.common.util.orNull
import site.remlit.aster.db.Database
import site.remlit.aster.db.entity.DriveFileEntity
import site.remlit.aster.db.entity.InviteEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.DriveFileTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.PackageInformation
import site.remlit.aster.model.Service
import java.lang.management.ManagementFactory
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

/**
 * Service for managing Aster via the command line.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object CommandLineService : Service {
	private val logger = LoggerFactory.getLogger(CommandLineService::class.java)

	fun help() {
		logger.info("${PackageInformation.name} ${PackageInformation.version}")
		logger.info("Run without arguments to start server")
		logger.info("help					Show this page")
		logger.info("files:clean					Clean up drive files that are no longer in the file storage")
		logger.info("files:generateblurhashes					Generate blur hashes for all media")
		logger.info("migration:generate			Generate migrations (for developer use)")
		logger.info("migration:execute			Execute migrations")
		logger.info("role:list				List all roles")
		logger.info("role:create				Create a role")
		logger.info("role:give				Give role to user")
		logger.info("role:revoke				Revoke role from user")
		logger.info("invite:generate			Generate invite")
	}

	@Suppress("MagicNumber")
	fun printDebug(args: Array<String>) {
		logger.debug("Starting ${PackageInformation.groupId}.${PackageInformation.name} v${PackageInformation.version}")
		logger.debug("* Arguments: ${if (args.isEmpty()) "None provided" else args.joinToString(" ")}")
		logger.debug("* VM Arguments: ${ManagementFactory.getRuntimeMXBean().inputArguments.joinToString(" ")}")
		logger.debug("* Runtime Version: ${System.getProperty("java.vm.vendor")} ${System.getProperty("java.version")}" +
			" on ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
		logger.debug("* Max Memory: ${Runtime.getRuntime().maxMemory() / (1024 * 1024)} MB")
		logger.debug("* Local URL: http://${Configuration.host}:${Configuration.port}")
		logger.debug("* World URL: ${Configuration.url}")
	}

	suspend fun prompt() {
		val scanner = Scanner(System.`in`)
		val line = scanner.nextLine()

		logger.info("Starting prompt.")

		when (line) {
			"exit" -> scanner.close()
			else -> execute(line.split(" ").toTypedArray())
		}

		scanner.close()
	}

	suspend fun execute(args: Array<String>) {
		Database.connection

		if (args.isNotEmpty()) {
			when (args[0]) {
				"help" -> {
					help()
					return
				}

				"prompt" -> {
					prompt()
					return
				}

				"files:clean" -> {
					val files = DriveService.getMany(DriveFileTable.id neq "")

					logger.info("${files.size} drive files found")

					for (file in files) {
						val supposedPath = Path(
							file.src.replace(
								"${Configuration.url.protocol.name}://${Configuration.url.host}/uploads",
								Configuration.fileStorage.localPath.absolutePathString()
							)
						)

						if (supposedPath.exists()) {
							logger.debug(
								"File {} ({}) found at {}",
								file.src,
								file.id,
								supposedPath
							)
						} else {
							logger.info(
								"File ${file.src} (${file.id}) missing, deleting"
							)
							DriveService.delete(DriveFileTable.id eq file.id)
						}
					}
				}

				"files:generateblurhashes" -> {
					fun generateBlurHash(url: String): String? {
						return orNull {
							val url = Url(url)

							println(url)

							if (url.host != Configuration.url.host || !url.fullPath.startsWith("/uploads"))
								return null

							return DriveService.generateBlurHash(Path(Configuration.fileStorage.localPath.toString() +
								url.fullPath.replace("uploads/", "")))
						}
					}

					val files = DriveService.getMany(UserTable.host eq null and
						(DriveFileTable.blurHash eq null))

					files.forEach { file ->
						if (!file.type.startsWith("image"))
							return@forEach

						val hash = generateBlurHash(file.src)
						logger.info("Generated blurhash $hash for file ${file.id}")

						transaction {
							DriveFileEntity.findByIdAndUpdate(file.id) {
								it.blurHash = hash
							}
						}

						UserService.getMany(UserTable.avatar eq file.src).forEach { user ->
							logger.info("Found avatar for user ${user.id} with same source, adding blurhash")
							transaction {
								UserEntity.findByIdAndUpdate(user.id.toString()) {
									it.avatarBlurHash = hash
								}
							}
						}

						UserService.getMany(UserTable.banner eq file.src).forEach { user ->
							logger.info("Found banner for user ${user.id} with same source, adding blurhash")
							transaction {
								UserEntity.findByIdAndUpdate(user.id.toString()) {
									it.bannerBlurHash = hash
								}
							}
						}
					}

					return
				}

				"migration:generate" -> {
					MigrationService.generate()
					return
				}

				"migration:execute" -> {
					MigrationService.execute()
					return
				}

				"role:list" -> {
					val roles = RoleService.getAll()

					for (role in roles) {
						logger.info("${role.id}	${role.name}	(${role.type})	${role.createdAt}	${role.updatedAt}")
					}
					return
				}

				"role:create" -> throw NotImplementedError()

				"role:give" -> {
					val userId = args[1]
					val roleId = args[2]

					if (userId.isEmpty())
						throw IllegalArgumentException("User ID required as second argument")

					if (roleId.isEmpty())
						throw IllegalArgumentException("Role ID required as third argument")

					val user = UserService.getById(userId)
						?: throw IllegalArgumentException("User $userId not found")

					val role = RoleService.getById(roleId)
						?: throw IllegalArgumentException("Role $roleId not found")

					transaction {
						val roles = user.roles.toMutableList()
						roles.add(roleId)

						user.roles = roles.toList()
						user.flush()
					}

					logger.info("Gave role ${role.name} to ${user.displayName ?: user.username}")
				}

				"role:revoke" -> {
					val userId = args[1]
					val roleId = args[2]

					if (userId.isEmpty())
						throw IllegalArgumentException("User ID required as second argument")

					if (roleId.isEmpty())
						throw IllegalArgumentException("Role ID required as third argument")

					val user = UserService.getById(userId)
						?: throw IllegalArgumentException("User $userId not found")

					val role = RoleService.getById(roleId)
						?: throw IllegalArgumentException("Role $roleId not found")

					transaction {
						val roles = user.roles.toMutableList()
						roles.remove(roleId)

						user.roles = roles
						user.storeWrittenValues()
					}

					logger.info("Revoked role ${role.name} from ${user.displayName ?: user.username}")
				}

				"invite:generate" -> {
					val instanceActor = UserService.getInstanceActor()

					val id = IdentifierService.generate()
					val code = RandomService.generateString()

					transaction {
						InviteEntity.new(id) {
							this.code = code
							creator = instanceActor
						}
					}

					logger.info("Created new invite: $code")
				}

				else -> println("Unknown command ${args[0]}. Run with 'help' for commands.")
			}
		}
	}
}
