rootProject.name = "aster"
gradle.extra.set("rootVersion", "2025.12.2.2-SNAPSHOT")

pluginManagement {
	plugins {
		application
		`maven-publish`

		kotlin("jvm") version "2.2.21"
		kotlin("multiplatform") version "2.2.21"
		kotlin("plugin.serialization") version "2.2.21"

		id("io.ktor.plugin") version "3.3.2"
		id("com.gradleup.shadow") version "8.3.0"
		id("org.jetbrains.dokka") version "2.0.0"

		id("io.gitlab.arturbosch.detekt") version "1.23.8"
	}
}

include("common")
include("common-generators")

include("mfmkt")
