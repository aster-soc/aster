plugins {
	`maven-publish`

	kotlin("multiplatform")
	kotlin("plugin.serialization")

	id("org.jetbrains.dokka")
}

group = "site.remlit"
version = "1.0.0-SNAPSHOT"

repositories {
	mavenCentral()
}

kotlin {
	jvm()
	jvmToolchain(21)

	js(IR) {
		nodejs()
		useEsModules()
		generateTypeScriptDefinitions()
		binaries.library()
		outputModuleName.set("mfmkt")
	}

	sourceSets.all {
		dependencies {
			implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
		}
	}

	sourceSets.commonTest {
		dependencies {
			implementation(kotlin("test"))
		}
	}

	compilerOptions {
		freeCompilerArgs.add("-opt-in=kotlin.js.ExperimentalJsExport")
		freeCompilerArgs.add("-opt-in=kotlin.js.ExperimentalWasmJsInterop")
	}
}
