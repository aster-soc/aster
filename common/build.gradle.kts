plugins {
	`maven-publish`

	kotlin("multiplatform")
	kotlin("plugin.serialization")

	id("org.jetbrains.dokka")
}

group = "site.remlit.aster"
version = gradle.extra.get("rootVersion") as String

repositories {
	mavenCentral()
	maven {
		url = uri("https://repo.remlit.site/releases")
	}
	maven {
		url = uri("https://repo.remlit.site/snapshots")
	}
}

kotlin {
	jvm()
	jvmToolchain(21)

	js(IR) {
		nodejs()
		useEsModules()
		generateTypeScriptDefinitions()
		binaries.library()
		outputModuleName.set("aster-common")
	}

	sourceSets {
		commonMain.dependencies {
			implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
			implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
		}
	}

	compilerOptions {
		freeCompilerArgs.add("-Xes-long-as-bigint")
		freeCompilerArgs.add("-opt-in=kotlin.js.ExperimentalJsExport")
	}
}

// docs

val commonSourcesJar by tasks.registering(Jar::class) {
	archiveBaseName = project.name
	archiveClassifier = "sources"
}

val commonDokkaHtmlZip by tasks.registering(Zip::class) {
	archiveBaseName = project.name
	archiveClassifier = "dokka"
	dependsOn(tasks.dokkaHtml)
	from(tasks.dokkaHtml.map { it.outputDirectory })
}

artifacts {
	add("archives", commonSourcesJar)
	add("archives", commonDokkaHtmlZip)
}

// publishing

tasks.publish {
	dependsOn("commonSourcesJar")
	dependsOn("commonDokkaHtmlZip")
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
			groupId = "site.remlit.aster"
			artifactId = "common"
			version = project.version.toString()

			artifact(commonSourcesJar)
			artifact(commonDokkaHtmlZip)

			pom {
				name = "common"
				url = "https://github.com/aster-soc/aster"

				licenses {
					license {
						name = "AGPLv3 License"
						url = "https://opensource.org/license/agpl-v3"
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
