package site.remlit.aster.route.admin

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.head
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.styleLink
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.title
import kotlinx.html.tr
import org.jetbrains.exposed.v1.core.neq
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.db.table.InstanceTable
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.InstanceService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.webcomponent.adminHeader
import site.remlit.aster.util.webcomponent.adminListNav
import site.remlit.aster.util.webcomponent.adminMain

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

					val instances = InstanceService.getMany(
						InstanceTable.id neq "",
						take,
						offset
					)
					val totalInstances = InstanceService.count(
						InstanceTable.id neq ""
					)

					call.respondHtml(HttpStatusCode.OK) {
						head {
							title { +"Instances" }
							styleLink("/admin/assets/index.css")
							script { src = "/admin/assets/index.js" }
						}
						body {
							adminHeader("Instances")
							adminMain {
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
												button {
													+"Suspend"
												}
											}
										}
									}
								}
								p {
									+"${instances.size} instances shown, $totalInstances total."
								}
								adminListNav(offset, take)
							}
						}
					}
				}
			}
		}
}
