package site.remlit.aster.registry

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.annotations.ApiStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.plugin.AsterPlugin
import site.remlit.aster.model.plugin.PluginManifest
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

object PluginRegistry {
	private val logger: Logger = LoggerFactory.getLogger(PluginRegistry::class.java)

	/**
	 * List of currently enabled plugins
	 * */
	@JvmStatic
	val plugins: MutableList<Pair<PluginManifest, AsterPlugin>> =
		emptyList<Pair<PluginManifest, AsterPlugin>>().toMutableList()

    /**
     * Directory plugins are expected to be stored in
     * */
    @JvmStatic
    val pluginDir = Path("plugins")

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

                            val mainClass = classLoader.loadClass(manifest.mainClass)
                            enablePlugin(
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

	/**
	 * Adds plugin to registry and runs it's enable hook.
	 *
	 * @param plugin Plugin to enable
	 * */
	@ApiStatus.Internal
	fun enablePlugin(manifest: PluginManifest, plugin: AsterPlugin) {
		if (
			plugins.find { it.first == manifest } != null ||
			plugins.find { it.second == plugin } != null
		) throw IllegalStateException("Attempted to register duplicate plugin")

		plugins.add(manifest to plugin)
		plugin.enable()

		logger.info("Enabled plugin ${manifest.name} (${manifest.version}) by ${manifest.authors.joinToString(", ")}")
	}

	/**
	 * Removes plugin to registry and runs it's disable hook.
	 *
	 * @param plugin Plugin to disable
	 * */
	@ApiStatus.Internal
	fun disablePlugin(plugin: AsterPlugin) {
		val pair = plugins.find { it.second == plugin }
		if (pair == null) return

		plugin.disable()
		plugins.remove(pair)

		logger.info("Disabled plugin ${pair.first.name} (${pair.first.version})")
	}

	/**
	 * Disables all currently active plugins
	 * */
	@ApiStatus.Internal
	fun disableAll() {
		try {
			for (plugin in plugins) {
				disablePlugin(plugin.second)
			}
		} catch (_: Throwable) {
		}
	}

	/**
	 * Disables all currently active plugins
	 * */
	@ApiStatus.Internal
	fun reloadAll() {
		disableAll()
        initialize()
	}
}
