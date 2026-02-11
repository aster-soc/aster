package site.remlit.aster.route.admin

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.classes
import kotlinx.html.p
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.neq
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.db.table.InstanceTable
import site.remlit.aster.model.config.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.InstanceService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.webcomponent.adminButton
import site.remlit.aster.util.webcomponent.adminListNav
import site.remlit.aster.util.webcomponent.adminPage

internal object AdminInstanceRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Admin
			) {
				get("/admin/instances") {
					val take = Configuration.timeline.defaultObjects
					val offset = call.parameters["offset"]?.toLong() ?: 0
					val query = call.request.queryParameters["q"]

					val instances = InstanceService.getMany(
						if (query != null) InstanceTable.host like "%$query%" else InstanceTable.id neq "",
						take,
						offset
					)
					val totalInstances = InstanceService.count(
						InstanceTable.id neq ""
					)

					call.respondHtml(HttpStatusCode.OK) {
						adminPage(call.route.path) {
							table {
								tr {
									classes = setOf("header")
									th { +"Host" }
									th { +"Software" }
									th { +"Description" }
									th { +"Actions" }
								}
								for (instance in instances) {
									tr {
										td { +instance.host }
										td { +"${instance.software} ${instance.version}" }
										td { +(instance.description.orEmpty()) }
										td {
											adminButton({ "" }) {
												+"Block"
											}
										}
									}
								}
							}
							p {
								+"${instances.size} instances shown, $totalInstances total."
							}
							adminListNav(offset, take, query)
						}
					}
				}
			}
		}
}
