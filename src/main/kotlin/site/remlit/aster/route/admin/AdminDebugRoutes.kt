package site.remlit.aster.route.admin

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.InputType
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.code
import kotlinx.html.head
import kotlinx.html.input
import kotlinx.html.pre
import kotlinx.html.script
import kotlinx.html.styleLink
import kotlinx.html.title
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.ResolverService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.webcomponent.adminHeader
import site.remlit.aster.util.webcomponent.adminMain

internal object AdminDebugRoutes {
	fun register() = RouteRegistry.registerRoute {
		authentication(
			required = true,
			role = RoleType.Admin
		) {
			get("/admin/debug") {
				val mode = call.request.queryParameters["mode"]
				val target = call.request.queryParameters["target"]
				call.respondHtml(HttpStatusCode.OK) {
					head {
						title { +"Admin Panel" }
						styleLink("/admin/assets/index.css")
						script { src = "/admin/assets/index.js" }
					}
					body {
						adminHeader("Debug")
						adminMain {
							when (mode) {
								"resolve" -> {
									if (target == null) {
										+"No target query parameter specified."
									} else {
										runBlocking {
											val response = ResolverService.resolveSigned(target)
											pre {
												code {
													+jsonConfig.encodeToString(response)
												}
											}
										}
									}
								}

								null -> {
									input {
										type = InputType.text
										placeholder = "https://example.com/note/000000000"
									}
									button {
										+"Resolve ID"
									}
								}

								else -> {
									+"Unknown mode."
								}
							}
						}
					}
				}
			}
		}
	}
}
