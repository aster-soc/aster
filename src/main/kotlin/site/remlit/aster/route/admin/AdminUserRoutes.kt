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
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.common.util.renderHandle
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.RoleService
import site.remlit.aster.service.UserService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.model.fromEntity
import site.remlit.aster.util.webcomponent.adminButton
import site.remlit.aster.util.webcomponent.adminListNav
import site.remlit.aster.util.webcomponent.adminPage

internal object AdminUserRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Admin
			) {
				get("/admin/users") {
					val take = Configuration.timeline.defaultObjects
					val offset = call.parameters["offset"]?.toLong() ?: 0
					val query = call.request.queryParameters["q"]
					val isLocal = true

					var where = (if (isLocal) UserTable.host eq null else UserTable.id neq null)

					if (query != null) where = where and (UserTable.username like "%$query%" or
						(UserTable.displayName like "%$query%"))

					val users = UserService.getMany(
						where,
						take,
						offset
					)

					val totalUsers = UserService.count(
						(if (isLocal) UserTable.host eq null else UserTable.id neq null)
					)

					call.respondHtml(HttpStatusCode.OK) {
						adminPage(call.route.path) {
							table {
								tr {
									classes = setOf("header")
									th { +"Username" }
									th { +"Status" }
									th { +"Actions" }
								}
								for (user in users) {
									tr {
										td {
											+renderHandle(User.fromEntity(user))
										}
										td {
											val status = mutableListOf<String>()

											status += if (user.activated) "Activated" else "Unactivated"

											val highestRole = RoleService.getUserHighestRole(user.id.toString())
											if (highestRole == RoleType.Admin) status += "Admin"
											if (highestRole == RoleType.Mod) status += "Mod"

											if (user.suspended) status += "Suspended"
											if (user.sensitive) status += "Sensitive"

											+status.joinToString(separator = ", ")
										}
										td {
											adminButton({ "" }) {
												+"Activate"
											}
											adminButton({ "" }) {
												+"Suspend"
											}
										}
									}
								}
							}
							p {
								+"${users.size}${if (isLocal) " local" else ""} users shown, $totalUsers total."
							}
							adminListNav(offset, take, query)
						}
					}
				}
			}
		}
}
