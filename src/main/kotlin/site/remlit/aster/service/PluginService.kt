package site.remlit.aster.service

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.annotations.ApiStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.model.plugin.AsterPlugin
import site.remlit.aster.model.plugin.PluginManifest
import site.remlit.aster.registry.PluginRegistry
import java.io.InputStream
import java.net.URI
import java.net.URLClassLoader
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * Service for managing plugins.
 *
 * @since 2025.9.1.1-SNAPSHOT
 * */
object PluginService : Service {
	private val logger: Logger = LoggerFactory.getLogger(PluginService::class.java)

	private val pluginDir = Path("plugins")

	/**
	 * Find and enable plugins in plugins directory.
	 * */
	@ApiStatus.Internal
	@OptIn(ExperimentalSerializationApi::class)
	fun initialize() {
		if (!pluginDir.exists()) pluginDir.createDirectories()

		pluginDir.listDirectoryEntries()
			.filter { it.extension == "jar" || it.isRegularFile() }
			.forEach { jar ->
				ZipFile(jar.toFile()).use { zip ->
					val pluginManifest = zip.getEntry("plugin.json")
					if (pluginManifest == null) {
						logger.warn("Plugin manifest missing for ${jar.name}, skipping")
						return@use
					}

					try {
						fun getInputStream(entry: ZipEntry): InputStream = zip.getInputStream(entry)

						getInputStream(pluginManifest).use { manifestStream ->
							val manifest = Json.decodeFromStream<PluginManifest>(manifestStream)

							val classLoader = URLClassLoader(
								arrayOf(URI("file://${jar.absolutePathString()}").toURL()),
								this::class.java.classLoader
							)

							if (Configuration.debug) logger.debug(
								"Current URLClassLoader URLS: {}",
								classLoader.urLs
							)

							val mainClass = classLoader.loadClass(manifest.mainClass)
							PluginRegistry.enablePlugin(
								manifest,
								mainClass.getDeclaredConstructor().newInstance() as AsterPlugin
							)
						}
					} catch (e: Throwable) {
						logger.error("Failed to load plugin ${jar.name}!", e)
					}
				}
			}
	}
}
