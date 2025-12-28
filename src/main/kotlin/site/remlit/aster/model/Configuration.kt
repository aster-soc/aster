package site.remlit.aster.model

import io.ktor.http.*
import io.ktor.server.config.yaml.*
import site.remlit.aster.common.model.type.FileStorageType
import site.remlit.aster.common.model.type.InstanceRegistrationsType
import site.remlit.aster.exception.ConfigurationException
import site.remlit.aster.service.TimeService
import site.remlit.aster.util.capitalize
import java.io.File
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

val version = System.getenv("CONFIG_VERSION")
var workingDir = File(".").absolutePath.toString().removeSuffix(".")
var configPath = if (version == "test") workingDir + "configuration.test.yaml" else workingDir + "configuration.yaml"
var config = YamlConfig(configPath)

var lastConfigReloadAt = TimeService.now()

interface ConfigurationObject

@Suppress("MagicNumber")
object Configuration : ConfigurationObject {
	val name: String get() = config?.propertyOrNull("name")?.getString() ?: "Aster"
    val description: String? get() = config?.propertyOrNull("description")?.getString()
    val color: String get() = config?.propertyOrNull("color")?.getString() ?: "#140e1b"

	val maintainer: ConfigurationMaintainer = ConfigurationMaintainer(
		config?.propertyOrNull("maintainer.name")?.getString() ?: "unknown",
		config?.propertyOrNull("maintainer.email")?.getString() ?: "unknown",
	)

	val url: Url
		get() =
			Url(
				config?.propertyOrNull("url")?.getString()
					?: throw ConfigurationException("Configuration is missing 'url' attribute.")
			)

	val port: Int get() = config?.propertyOrNull("port")?.getString()?.toInt() ?: 9782
	val host: String get() = config?.propertyOrNull("host")?.getString() ?: "0.0.0.0"

	val debug: Boolean get() = config?.propertyOrNull("debug")?.getString()?.toBoolean() ?: false
	val builtinFrontend: Boolean get() = config?.propertyOrNull("builtinFrontend")?.getString()?.toBoolean() ?: true

	val registrations: InstanceRegistrationsType
		get() =
			InstanceRegistrationsType.valueOf(
				config?.propertyOrNull("registrations")?.getString()?.capitalize() ?: "Closed"
			)
	val identifiers: IdentifierType
		get() =
			IdentifierType.valueOf(config?.propertyOrNull("identifiers")?.getString()?.capitalize() ?: "Aidx")

	val database: ConfigurationDatabase = ConfigurationDatabase()
	val queue: ConfigurationQueue = ConfigurationQueue()
	val timeline: ConfigurationTimeline = ConfigurationTimeline()
	val fileStorage: ConfigurationFileStorage = ConfigurationFileStorage()

	val hideRemoteContent: Boolean get() = config?.propertyOrNull("hideRemoteContent")?.getString()?.toBoolean() ?: true
	val maxResolveDepth: Int get() = config?.propertyOrNull("maxResolveDepth")?.getString()?.toInt() ?: 20

	val reservedUsernames: List<String>
		get() =
			config?.propertyOrNull("reservedUsernames")?.getList().orEmpty()

	init {
		thread(name = "Configuration Refresher") {
			while (true) {
				Thread.sleep(30 * 1000L)
				config = YamlConfig(configPath)
				lastConfigReloadAt = TimeService.now()
			}
		}
	}
}

data class ConfigurationMaintainer(
	val name: String,
	val email: String,
) : ConfigurationObject

@Suppress("MagicNumber")
class ConfigurationFileStorage : ConfigurationObject {
	val type: FileStorageType
		get() = FileStorageType.valueOf(
			config?.propertyOrNull("fileStorage.type")?.getString()?.capitalize() ?: "Local"
		)
	val localPath: Path
		get() {
			val path = Path(config?.propertyOrNull("fileStorage.localPath")?.getString() ?: "/var/lib/aster/files")

			if (!path.exists())
				throw ConfigurationException("File storage path doesn't exist")
			if (!path.isDirectory())
				throw ConfigurationException("File storage path is a file, not a directory")
			if (!path.toFile().canWrite())
				throw ConfigurationException("File storage path is not writeable")

			return path
		}

	val maxUploadSize: Int
		get() = config?.propertyOrNull("fileStorage.maxUploadSize")?.getString()?.toInt() ?: 25
}

@Suppress("MagicNumber")
class ConfigurationDatabase : ConfigurationObject {
	val host: String get() = config?.propertyOrNull("database.host")?.getString() ?: "127.0.0.1"
	val port: String get() = config?.propertyOrNull("database.port")?.getString() ?: "5432"
	val db: String
		get() = config?.propertyOrNull("database.db")?.getString()
			?: throw ConfigurationException("Configuration is missing 'database.db' attribute.")
	val user: String
		get() = config?.propertyOrNull("database.user")?.getString()
			?: throw ConfigurationException("Configuration is missing 'database.user' attribute.")
	val password: String
		get() = config?.propertyOrNull("database.password")?.getString()
			?: throw ConfigurationException("Configuration is missing 'database.password' attribute.")
}

@Suppress("MagicNumber")
class ConfigurationQueue : ConfigurationObject {
	val inbox: ConfigurationSpecificQueue
		get() = ConfigurationSpecificQueue(
			(config?.propertyOrNull("queue.inbox.concurrency")?.getString()?.toInt() ?: 8),
			((config?.propertyOrNull("queue.deliver.maxRetries")?.getString()?.toInt()) ?: 10)
		)
	val deliver: ConfigurationSpecificQueue
		get() = ConfigurationSpecificQueue(
			((config?.propertyOrNull("queue.deliver.concurrency")?.getString()?.toInt()) ?: 6),
			((config?.propertyOrNull("queue.deliver.maxRetries")?.getString()?.toInt()) ?: 15)
		)
}

data class ConfigurationSpecificQueue(
	val concurrency: Int,
	val maxRetries: Int
) : ConfigurationObject

@Suppress("MagicNumber")
class ConfigurationTimeline : ConfigurationObject {
	val defaultObjects: Int get() = config?.propertyOrNull("timeline.defaultObjects")?.getString()?.toIntOrNull() ?: 15
	val maxObjects: Int get() = config?.propertyOrNull("timeline.maxObjects")?.getString()?.toIntOrNull() ?: 35

	val local: ConfigurationSpecificTimeline
		get() = ConfigurationSpecificTimeline(
			authRequired = (config?.propertyOrNull("timeline.local.authRequired")?.getString()?.toBooleanStrictOrNull()
				?: false)
		)
	val bubble: ConfigurationBubbleTimeline
		get() = ConfigurationBubbleTimeline(
			authRequired = (config?.propertyOrNull("timeline.bubble.authRequired")?.getString()?.toBooleanStrictOrNull()
				?: false),
			hosts = (config?.propertyOrNull("timeline.bubble.hosts")?.getList().orEmpty())
		)
	val public: ConfigurationSpecificTimeline
		get() = ConfigurationSpecificTimeline(
			authRequired = (config?.propertyOrNull("timeline.public.authRequired")?.getString()?.toBooleanStrictOrNull()
				?: false)
		)
}

data class ConfigurationSpecificTimeline(
	val authRequired: Boolean,
) : ConfigurationObject

data class ConfigurationBubbleTimeline(
	val authRequired: Boolean,
	val hosts: List<String>,
) : ConfigurationObject
