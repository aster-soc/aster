package site.remlit.aster.registry

import org.jetbrains.annotations.ApiStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import site.remlit.aster.model.plugin.AsterPlugin
import site.remlit.aster.model.plugin.PluginManifest
import site.remlit.aster.service.PluginService

object PluginRegistry {
	private val logger: Logger = LoggerFactory.getLogger(PluginRegistry::class.java)

	/**
	 * List of currently enabled plugins
	 * */
	@JvmStatic
	val plugins: MutableList<Pair<PluginManifest, AsterPlugin>> =
		emptyList<Pair<PluginManifest, AsterPlugin>>().toMutableList()

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
		} catch (_: Exception) {
		}
	}

	/**
	 * Disables all currently active plugins
	 * */
	@ApiStatus.Internal
	fun reloadAll() {
		disableAll()
		PluginService.initialize()
	}
}
