@file:OptIn(OpenApiPreview::class)

import io.ktor.plugin.*
import org.gradle.internal.extensions.stdlib.capitalized

plugins {
	application
	`maven-publish`

	kotlin("jvm")
	kotlin("plugin.serialization")

	id("io.ktor.plugin")
	id("com.gradleup.shadow")
	id("org.jetbrains.dokka")
	id("org.jetbrains.dokka-javadoc")

	id("io.gitlab.arturbosch.detekt")
}

group = "site.remlit"
version = gradle.extra.get("rootVersion") as String

repositories {
	mavenCentral()
	maven("https://repo.remlit.site/releases")
	maven("https://repo.remlit.site/snapshots")
}

dependencies {
	implementation("ch.qos.logback:logback-classic:1.5.20")
	implementation("org.slf4j:slf4j-api:2.0.17")

	// ktor server
	implementation("io.ktor:ktor-server-core-jvm:3.4.0")
	implementation("io.ktor:ktor-server-netty-jvm:3.4.0")
	implementation("io.ktor:ktor-server-csrf-jvm:3.4.0")
	implementation("io.ktor:ktor-server-config-yaml-jvm:3.4.0")
	implementation("io.ktor:ktor-server-call-logging-jvm:3.4.0")
	implementation("io.ktor:ktor-server-request-validation-jvm:3.4.0")
	implementation("io.ktor:ktor-server-call-id-jvm:3.4.0")
	implementation("io.ktor:ktor-server-cors-jvm:3.4.0")
	implementation("io.ktor:ktor-server-call-logging-jvm:3.4.0")
	implementation("io.ktor:ktor-server-default-headers-jvm:3.4.0")
	implementation("io.ktor:ktor-server-forwarded-header-jvm:3.4.0")
	implementation("io.ktor:ktor-server-routing-openapi:3.4.0")
	implementation("io.ktor:ktor-server-openapi-jvm:3.4.0")
	implementation("io.ktor:ktor-server-swagger-jvm:3.4.0")
	implementation("io.ktor:ktor-server-status-pages-jvm:3.4.0")
	implementation("io.ktor:ktor-server-auto-head-response-jvm:3.4.0")
	implementation("io.ktor:ktor-server-double-receive-jvm:3.4.0")
	implementation("io.ktor:ktor-server-websockets-jvm:3.4.0")

	// templating
	implementation("io.ktor:ktor-server-html-builder-jvm:3.4.0")
	implementation("org.jetbrains.kotlinx:kotlinx-html:0.12.0")

	// serialization
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.9.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.9.0")
	implementation("io.ktor:ktor-server-content-negotiation-jvm:3.4.0")
	implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.4.0")

	// ktor client
	implementation("io.ktor:ktor-client-core-jvm:3.4.0")
	implementation("io.ktor:ktor-client-cio-jvm:3.4.0")
	implementation("io.ktor:ktor-client-content-negotiation-jvm:3.4.0")

	// database
	implementation("com.zaxxer:HikariCP:7.0.2")
	implementation("org.postgresql:postgresql:42.7.8")
	implementation("org.jetbrains.exposed:exposed-core:1.0.0-rc-4")
	implementation("org.jetbrains.exposed:exposed-dao:1.0.0-rc-4")
	implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-rc-4")
	implementation("org.jetbrains.exposed:exposed-json:1.0.0-rc-4")
	implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.0.0-rc-4")
	implementation("org.jetbrains.exposed:exposed-migration-core:1.0.0-rc-4")
	implementation("org.jetbrains.exposed:exposed-migration-jdbc:1.0.0-rc-4")

	// authentication
	implementation("at.favre.lib:bcrypt:0.10.2")
	implementation("com.j256.two-factor-auth:two-factor-auth:1.3")

	// misc
	implementation("io.trbl:blurhash:1.0.0")
	implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.0")
	implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20260101.1")
	implementation("site.remlit:aidx4j:1.0.0")
	implementation("site.remlit:effekt:0.2.1")

	compileOnly("org.jetbrains:annotations:26.0.2-1")

	// test
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
	testImplementation("io.ktor:ktor-server-test-host:2.2.21")
	testImplementation(kotlin("test"))

	api(project(":common"))
	api(project(":mfmkt"))
}

kotlin {
	jvmToolchain(21)
}

tasks.withType<Test>().configureEach {
	environment("CONFIG_VERSION", "test")
}

application {
	mainClass = "site.remlit.aster.ApplicationKt"
	applicationDefaultJvmArgs = listOf(
		"-XX:+UseZGC",
		"-XX:+UseDynamicNumberOfGCThreads",
		"-Dsite.remlit.aster=true"
	)
}

ktor {
	openApi {
		enabled = true
		codeInferenceEnabled = true
		onlyCommented = false
	}
}

// style

detekt {
	toolVersion = "1.23.8"
	config.setFrom(file("detekt.yml"))
	buildUponDefaultConfig = true
}

if ("detekt" !in gradle.startParameter.taskNames) {
	tasks.detekt { enabled = false }
}

tasks.distTar { enabled = false }
tasks.distZip { enabled = false }
tasks.shadowDistTar { enabled = false }
tasks.shadowDistZip { enabled = false }

// building

tasks.register("preCommit") {
	dependsOn("detekt")
	dependsOn("test")
}

tasks.clean {
	dependsOn(":frontend:clean")
	delete("src/main/resources/frontend")
	delete("src/main/resources/admin/aster-common")
}

tasks.register<Copy>("copyFrontend") {
	dependsOn(":frontend:build")
	from("frontend/packages/app/dist")
	into("src/main/resources/frontend")
}

tasks.processResources {
	dependsOn("copyFrontend")

	val name = project.provider { project.name }.get()
	val group = project.provider { project.group.toString() }.get()
	val version = project.provider { project.version.toString() }.get()

	val repo = gradle.extra.get("repository") as String
	val bugTracker = gradle.extra.get("issueTracker") as String

	filesMatching("application.yaml") {
		filter { line ->
			line.replace("%artifactId%", name)
				.replace("%version%", version)
				.replace("%groupId%", group)
				.replace("%repo%", repo)
				.replace("%bugTracker%", bugTracker)
		}
	}
}

tasks.shadowJar {
	archiveFileName.set("${project.name}-${project.version}-all.jar")
	dependsOn("processResources")
}

tasks.build {
	dependsOn("shadowJar")
}

// docs

dokka {
	dokkaPublications.html {
		moduleName.set(project.name.capitalized())
	}
}

val dokkaZip by tasks.registering(Zip::class) {
	dependsOn("dokkaGenerateHtml")
	archiveClassifier.set("dokka")
	from(layout.buildDirectory.dir("dokka/html"))
}

val javadocJar by tasks.registering(Jar::class) {
	dependsOn("dokkaGenerateJavadoc")
	archiveClassifier.set("javadoc")
	from(layout.buildDirectory.dir("dokka/javadoc"))
}

val javadocZip by tasks.registering(Zip::class) {
	dependsOn("dokkaGenerateJavadoc")
	archiveClassifier.set("javadoc")
	from(layout.buildDirectory.dir("dokka/javadoc"))
}

val sourcesJar by tasks.registering(Jar::class) {
	mustRunAfter("processResources")
	archiveClassifier.set("sources")
	from(sourceSets.main.get().allSource)
}

// publishing

tasks.publish {
	dependsOn(":common:publish")
	dependsOn(":mfmkt:publish")

	dependsOn(":dokkaGenerate")
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
			artifactId = "aster"
			version = project.version.toString()

			from(components["java"])

			artifact(dokkaZip)
			artifact(javadocJar)
			artifact(javadocZip)
			artifact(sourcesJar)

			pom {
				name = "aster"
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
