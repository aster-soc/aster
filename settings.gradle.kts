rootProject.name = "aster"
gradle.extra.set("rootVersion", "2025.11.4.0-SNAPSHOT")

pluginManagement {
	plugins {
		application
		`maven-publish`

		kotlin("jvm") version "2.3.0-RC"
		kotlin("multiplatform") version "2.3.0-RC"
		kotlin("plugin.serialization") version "2.3.0-RC"

		id("io.ktor.plugin") version "3.3.2"
		id("com.gradleup.shadow") version "8.3.0"
		id("org.jetbrains.dokka") version "2.0.0"

		id("io.gitlab.arturbosch.detekt") version "1.23.8"
	}
}

include("common")
include("common-generators")
