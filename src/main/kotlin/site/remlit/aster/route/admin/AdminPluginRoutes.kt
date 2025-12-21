package site.remlit.aster.route.admin

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.b
import kotlinx.html.classes
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.ul
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.registry.PluginRegistry
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.util.authentication
import site.remlit.aster.util.webcomponent.adminButton
import site.remlit.aster.util.webcomponent.adminPage

internal object AdminPluginRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Admin
			) {
				get("/admin/plugins") {
					call.respondHtml(HttpStatusCode.OK) {
						adminPage(call.route.path) {
							adminButton({ "reloadPlugins()" }) {
								+"Reload Plugins"
							}
							div {
								this.classes = setOf("ctn")
								div {
									this.classes = setOf("ctn", "column")
									ul {
										for (plugin in PluginRegistry.plugins) {
											li {
												b { +"${plugin.first.name} (${plugin.first.version})" }
												p { code { +plugin.first.mainClass } }
												div {
													plugin.first.adminPages.forEach { (name, href) ->
														adminButton(href) { +name }
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
}
