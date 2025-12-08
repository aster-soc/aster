import io.ktor.plugin.*

plugins {
	application
	`maven-publish`

	kotlin("jvm")
	kotlin("plugin.serialization")

	id("io.ktor.plugin")
	id("com.gradleup.shadow")
	id("org.jetbrains.dokka")

	id("io.gitlab.arturbosch.detekt")
}

group = "site.remlit"
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

dependencies {
	implementation("ch.qos.logback:logback-classic:1.5.20")
	implementation("org.slf4j:slf4j-api:2.0.17")

	// ktor server
	implementation("io.ktor:ktor-server-core-jvm:3.3.3")
	implementation("io.ktor:ktor-server-netty-jvm:3.3.3")
	implementation("io.ktor:ktor-server-csrf-jvm:3.3.3")
	implementation("io.ktor:ktor-server-config-yaml-jvm:3.3.3")
	implementation("io.ktor:ktor-server-call-logging-jvm:3.3.3")
	implementation("io.ktor:ktor-server-request-validation-jvm:3.3.3")
	implementation("io.ktor:ktor-server-call-id-jvm:3.3.3")
	implementation("io.ktor:ktor-server-cors-jvm:3.3.3")
	implementation("io.ktor:ktor-server-call-logging-jvm:3.3.3")
	implementation("io.ktor:ktor-server-default-headers-jvm:3.3.3")
	implementation("io.ktor:ktor-server-forwarded-header-jvm:3.3.3")
	implementation("io.ktor:ktor-server-openapi-jvm:3.3.3")
	implementation("io.ktor:ktor-server-swagger-jvm:3.3.3")

	// templating
	implementation("io.ktor:ktor-server-html-builder-jvm:3.3.3")
	implementation("org.jetbrains.kotlinx:kotlinx-html:0.12.0")

	// serialization
	implementation("io.ktor:ktor-server-content-negotiation-jvm:3.3.3")
	implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.3.3")

	// ktor client
	implementation("io.ktor:ktor-client-core-jvm:3.3.3")
	implementation("io.ktor:ktor-client-cio-jvm:3.3.3")
	implementation("io.ktor:ktor-server-status-pages-jvm:3.3.3")
	implementation("io.ktor:ktor-server-auto-head-response-jvm:3.3.3")
	implementation("io.ktor:ktor-client-content-negotiation-jvm:3.3.3")

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

	// misc
	implementation("at.favre.lib:bcrypt:0.10.2")
	implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20240325.1")
	implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.21")

	compileOnly("org.jetbrains:annotations:26.0.2-1")
	testImplementation(kotlin("test"))

	api(project(":common"))
	api(project(":mfmkt"))
}

kotlin {
	jvmToolchain(21)
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
	@OptIn(OpenApiPreview::class)
	openApi {
		title = project.name
		version = project.version.toString()
		target = project.layout.buildDirectory.file("openapi.json")
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

// docs

val sourcesJar by tasks.registering(Jar::class) {
	archiveBaseName = project.name
	archiveClassifier = "sources"
	mustRunAfter("copyFrontend")
}

val dokkaJavadocZip by tasks.registering(Zip::class) {
	archiveBaseName = project.name
	archiveClassifier = "javadoc"
	dependsOn(tasks.dokkaJavadoc)
	from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
}

val dokkaHtmlZip by tasks.registering(Zip::class) {
	archiveBaseName = project.name
	archiveClassifier = "dokka"
	dependsOn(tasks.dokkaHtml)
	from(tasks.dokkaHtml.map { it.outputDirectory })
}

artifacts {
	add("archives", sourcesJar)
	add("archives", dokkaJavadocZip)
	add("archives", dokkaHtmlZip)
}

// building

tasks.register<Exec>("cleanFrontend") {
	executable("./scripts/clean-frontend.sh")
}

tasks.clean {
	dependsOn("cleanFrontend")
}

tasks.register<Exec>("compileFrontend") {
	dependsOn(":mfmkt:build")
	dependsOn(":common:build")
	executable("./scripts/build-frontend.sh")
}

tasks.register<Copy>("copyFrontend") {
	dependsOn("compileFrontend")
	from("frontend/packages/app/dist")
	into("src/main/resources/frontend")
}

tasks.processResources {
	dependsOn("copyFrontend")

	val name = project.provider { project.name }.get()
	val group = project.provider { project.group.toString() }.get()
	val version = project.provider { project.version.toString() }.get()

	val repo = "https://github.com/aster-soc/aster"
	val bugTracker = "$repo/issues"

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
	dependsOn("compileFrontend")
	dependsOn("processResources")
}

tasks.build {
	dependsOn("shadowJar")
}

// publishing

tasks.publish {
	dependsOn(":common:publish")
	dependsOn("sourcesJar")
	dependsOn("dokkaHtml")
	dependsOn("javadoc")
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

			artifact(sourcesJar)
			artifact(dokkaJavadocZip)
			artifact(dokkaHtmlZip)

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
