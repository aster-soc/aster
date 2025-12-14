package site.remlit.aster.route.admin

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.b
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.li
import kotlinx.html.onClick
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.styleLink
import kotlinx.html.title
import kotlinx.html.ul
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.registry.PluginRegistry
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.util.authentication
import site.remlit.aster.util.webcomponent.adminHeader
import site.remlit.aster.util.webcomponent.adminMain

internal object AdminPluginRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Admin
			) {
				get("/admin/plugins") {
					call.respondHtml(HttpStatusCode.OK) {
						head {
							title { +"Plugins" }
							styleLink("/admin/assets/index.css")
							script { src = "/admin/assets/index.js" }
						}
						body {
							adminHeader("Plugins")
							adminMain {
								button {
									classes = setOf("btn")
									onClick = "reloadPlugins()"
									+"Reload Plugins"
								}
								div {
									this.classes = setOf("ctn")
									div {
										this.classes = setOf("ctn", "column")
										ul {
											for (plugin in PluginRegistry.plugins) {
												li {
													b { +"${plugin.first.name} ${plugin.first.version}" }
													p { +plugin.first.mainClass }
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
}
