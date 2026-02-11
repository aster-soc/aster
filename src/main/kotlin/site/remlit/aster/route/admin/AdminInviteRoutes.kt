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
import site.remlit.aster.db.table.InviteTable
import site.remlit.aster.model.config.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.InviteService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.webcomponent.adminButton
import site.remlit.aster.util.webcomponent.adminListNav
import site.remlit.aster.util.webcomponent.adminPage

internal object AdminInviteRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Mod
			) {
				get("/admin/invites") {
					val take = Configuration.timeline.defaultObjects
					val offset = call.parameters["offset"]?.toLong() ?: 0
					val query = call.request.queryParameters["q"]

					val invites = InviteService.getMany(
						if (query == null) InviteTable.id neq "" else InviteTable.code like "%$query%",
						take,
						offset
					)
					val totalInvites = InviteService.count(
						InviteTable.id neq ""
					)

					call.respondHtml(HttpStatusCode.OK) {
						adminPage(call.route.path) {
							table {
								tr {
									classes = setOf("header")
									th { +"Invite" }
									th { +"Actions" }
								}
								for (invite in invites) {
									tr {
										td {
											classes = setOf("_75")
											+invite.code
										}
										td {
											adminButton({ "" }) {
												+"Delete"
											}
										}
									}
								}
							}
							p {
								+"${invites.size} invites shown, $totalInvites total."
							}
							adminListNav(offset, take, query)
						}
					}
				}
			}
		}
}
