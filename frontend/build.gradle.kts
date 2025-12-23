group = "site.remlit.aster"
version = gradle.extra.get("rootVersion") as String

plugins {
	id("org.siouan.frontend-jdk21") version "10.0.0"
}

frontend {
	nodeVersion = "24.12.0"
	installScript = "setup"
	assembleScript = "build"
}

tasks.clean {
	delete("packages/app/dist")
}

tasks.installFrontend {
	dependsOn(":common:build")
	dependsOn(":mfmkt:build")
}
