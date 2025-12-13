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

// docs

val mfmktSourcesJar by tasks.registering(Jar::class) {
	archiveBaseName = project.name
	archiveClassifier = "sources"
}

val mfmktDokkaHtmlZip by tasks.registering(Zip::class) {
	archiveBaseName = project.name
	archiveClassifier = "dokka"
	dependsOn(tasks.dokkaHtml)
	from(tasks.dokkaHtml.map { it.outputDirectory })
}

artifacts {
	add("archives", mfmktSourcesJar)
	add("archives", mfmktDokkaHtmlZip)
}

// publishing

tasks.publish {
	dependsOn("mfmktSourcesJar")
	dependsOn("mfmktDokkaHtmlZip")
}

publishing {
	repositories {
		maven {
			name = "remlitSiteMain"
			url = if (version.toString()
					.contains("SNAPSHOT")
			) uri("https://repo.remlit.site/snapshots") else uri("https://repo.remlit.site/releases")

			credentials {
				username = System.getenv("REPO_ACTOR")
				password = System.getenv("REPO_TOKEN")
			}
		}
	}
	publications {
		create<MavenPublication>("maven") {
			groupId = "site.remlit"
			artifactId = "mfmkt"
			version = project.version.toString()

			artifact(mfmktSourcesJar)
			artifact(mfmktDokkaHtmlZip)

			pom {
				name = "mfmkt"
				url = "https://github.com/aster-soc/aster"

				licenses {
					license {
						name = "GPLv3 License"
						url = "https://opensource.org/license/gpl-3-0"
					}
				}

				developers {
					developer {
						id = "ihateblueb"
						name = "ihateblueb"
						email = "ihateblueb@proton.me"
					}
				}

				scm {
					connection = "scm:git:git://github.com/aster-soc/aster.git"
					developerConnection = "scm:git:ssh://github.com/aster-soc/aster.git"
					url = "https://github.com/aster-soc/aster"
				}
			}
		}
	}
}
