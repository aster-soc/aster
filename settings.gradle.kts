rootProject.name = "aster"
gradle.extra.set("rootVersion", "2026.2.1.0-SNAPSHOT")
gradle.extra.set("repository", "https://git.remlit.site/aster/aster")
gradle.extra.set("issueTracker", "https://git.remlit.site/aster/aster/issues")

pluginManagement {
	plugins {
		application
		`maven-publish`

		kotlin("jvm") version "2.3.0"
		kotlin("multiplatform") version "2.3.0"
		kotlin("plugin.serialization") version "2.3.0"

		id("io.ktor.plugin") version "3.4.0"
		id("com.gradleup.shadow") version "9.3.0"
		id("org.jetbrains.dokka") version "2.1.0"
		id("org.jetbrains.dokka-javadoc") version "2.1.0"

		id("io.gitlab.arturbosch.detekt") version "1.23.8"
	}
}

include("frontend")

include("common")
include("common-generators")

include("mfmkt")
