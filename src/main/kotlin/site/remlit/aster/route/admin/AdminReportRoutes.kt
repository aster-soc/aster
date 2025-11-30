package site.remlit.aster.route.admin

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.a
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
import site.remlit.aster.db.table.ReportTable
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.ReportService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.webcomponent.adminHeader
import site.remlit.aster.util.webcomponent.adminListNav
import site.remlit.aster.util.webcomponent.adminMain

object AdminReportRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Mod
			) {
				get("/admin/reports") {
					val take = Configuration.timeline.defaultObjects
					val offset = call.parameters["offset"]?.toLong() ?: 0

					val reports = ReportService.getMany(
						ReportTable.id neq null,
						take,
						offset
					)

					val totalReports = ReportService.count(
						ReportTable.id neq null
					)

					call.respondHtml(HttpStatusCode.OK) {
						head {
							title { +"Reports" }
							styleLink("/admin/assets/index.css")
							script { src = "/admin/assets/index.js" }
						}
						body {
							adminHeader("Reports")
							adminMain {
								table {
									tr {
										classes = setOf("header")
										th { +"Sender" }
										th { +"Resolved" }
										th { +"Comment" }
										th { +"Target" }
										th { +"Actions" }
									}
									for (report in reports) {
										val isResolved = report.resolvedBy != null

										tr {
											td {
												+report.sender.renderHandle()
											}
											td {
												if (isResolved) +"Yes" else +"No"
											}
											td {
												+"${report.comment}"
											}
											td {
												if (report.note != null) {
													a {
														href = "/note/${report.note!!.id}"
														+"Note by ${report.note!!.user.renderHandle()}"
													}
												}

												if (report.user != null) {
													a {
														href = "/${report.user!!.renderHandle()}"
														+"User ${report.user!!.renderHandle()}"
													}
												}
											}
											td {
												if (!isResolved) {
													button {
														+"Resolve"
													}
												}
												button {
													+"Delete"
												}
											}
										}
									}
								}
								p {
									+"${reports.size} reports shown, $totalReports total."
								}
								adminListNav(offset, take)
							}
						}
					}
				}
			}
		}
}
