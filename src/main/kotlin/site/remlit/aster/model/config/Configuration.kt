package site.remlit.aster.model.config

import io.ktor.http.*
import io.ktor.server.config.yaml.*
import site.remlit.aster.common.model.type.InstanceRegistrationsType
import site.remlit.aster.exception.ConfigurationException
import site.remlit.aster.model.IdentifierType
import site.remlit.aster.service.TimeService
import site.remlit.aster.util.capitalize
import java.io.File
import kotlin.concurrent.thread

@Suppress("MagicNumber")
object Configuration : ConfigurationObject {
	private val version = System.getenv("CONFIG_VERSION")
	private var workingDir = File(".").absolutePath.toString().removeSuffix(".")
	private var configPath = if (version == "test") workingDir + "configuration.test.yaml" else workingDir + "configuration.yaml"
	private var config = YamlConfig(configPath)

	private var lastConfigReloadAt = TimeService.now()

	/* Configuration Begins */

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
	val note: ConfigurationNote = ConfigurationNote()
	val fileStorage: ConfigurationFileStorage = ConfigurationFileStorage()

	val hideRemoteContent: Boolean get() = config?.propertyOrNull("hideRemoteContent")?.getString()?.toBoolean() ?: true
	val maxResolveDepth: Int get() = config?.propertyOrNull("maxResolveDepth")?.getString()?.toInt() ?: 20

	val reservedUsernames: List<String>
		get() =
			config?.propertyOrNull("reservedUsernames")?.getList().orEmpty()

	val debug: Boolean get() = config?.propertyOrNull("debug")?.getString()?.toBoolean() ?: false
	val pauseInbox: Boolean get() = config?.propertyOrNull("pauseInbox")?.getString()?.toBoolean() ?: false

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
