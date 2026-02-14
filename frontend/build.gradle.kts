group = "site.remlit.aster"
version = gradle.extra.get("rootVersion") as String

tasks.register("clean") {
	delete("packages/app/dist")
}

tasks.register<Exec>("pnpm-i") {
	commandLine(System.getenv("SHELL"), "-c", "pnpm i")
}

tasks.register<Exec>("pnpm-setup") {
	dependsOn("pnpm-i")
	commandLine(System.getenv("SHELL"), "-c", "pnpm setup")
}

tasks.register<Exec>("pnpm-build") {
	dependsOn("pnpm-setup")
	commandLine(System.getenv("SHELL"), "-c", "pnpm build")
}

tasks.register("build") {
	dependsOn("pnpm-build")

	dependsOn(":common:build")
	dependsOn(":mfmkt:build")
}
