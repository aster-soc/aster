package site.remlit.aster.model

import io.ktor.server.config.*

private val appConfig = ApplicationConfig("application.yaml")

object PackageInformation {
	val name: String = appConfig.propertyOrNull("name")?.getString() ?: "aster"
	val version: String = appConfig.propertyOrNull("version")?.getString() ?: "0.0.0"
	val groupId: String = appConfig.propertyOrNull("groupId")?.getString() ?: "site.remlit"
	val repo: String = appConfig.propertyOrNull("repo")?.getString() ?: "https://git.remlit.site/aster/aster"
	val bugTracker: String =
		appConfig.propertyOrNull("bugTracker")?.getString() ?: "https://git.remlit.site/aster/aster/issues"
}
