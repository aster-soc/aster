package site.remlit.aster.route.admin

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.script
import kotlinx.html.styleLink
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.title
import kotlinx.html.tr
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.neq
import site.remlit.aster.common.model.Meta
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.db.table.NoteTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.DriveService
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.TimeService
import site.remlit.aster.service.UserService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.model.getMeta
import site.remlit.aster.util.webcomponent.adminHeader
import site.remlit.aster.util.webcomponent.adminMain

internal object AdminIndexRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			staticResources("/admin/assets", "admin")

			authentication(
				required = true,
				role = RoleType.Admin
			) {
				get("/admin") {
					call.respondHtml(HttpStatusCode.OK) {
						head {
							title { +"Admin Panel" }
							styleLink("/admin/assets/index.css")
							script { src = "/admin/assets/index.js" }
						}
						body {
							adminHeader("Overview")
							adminMain {
								val meta = Meta.getMeta()
								h2 { +"Software" }
								table {
									classes = setOf("_50")
									tr {
										th { +"Version" }
										td { +meta.version.aster }
									}
									tr {
										th { +"Java" }
										td { +meta.version.java }
									}
									tr {
										th { +"Kotlin" }
										td { +meta.version.kotlin }
									}
									tr {
										th { +"System" }
										td { +meta.version.system }
									}
								}
								h2 { +"Statistics" }
								table {
									tr {
										th { +"Known instances" }
										td { +"0" }
									}
									tr {
										th { +"Active instances" }
										td { +"0" }
									}

									val totalNotes = NoteService.count(NoteTable.id neq null)
									val localNotes = NoteService.count(UserTable.host eq null)

									tr {
										th { +"Total notes" }
										td { +totalNotes.toString() }
									}
									tr {
										th { +"Local notes" }
										td { +localNotes.toString() }
									}

									val totalNotes24hr =
										NoteService.count(NoteTable.createdAt greaterEq (TimeService.daysAgo(1)))
									val localNotes24hr =
										NoteService.count(
											UserTable.host eq null and
													(NoteTable.createdAt greaterEq (TimeService.daysAgo(1)))
										)

									tr {
										th { +"Total notes, past 24hr" }
										td { +totalNotes24hr.toString() }
									}
									tr {
										th { +"Local notes, past 24hr" }
										td { +localNotes24hr.toString() }
									}

									val totalUsers = UserService.count(UserTable.id neq null)
									val localUsers = UserService.count(UserTable.host eq null)

									tr {
										th { +"Total users" }
										td { +totalUsers.toString() }
									}
									tr {
										th { +"Local users" }
										td { +localUsers.toString() }
									}

									val totalFiles = DriveService.count(UserTable.id neq null)
									val localFiles = DriveService.count(UserTable.host eq null)

									tr {
										th { +"Total files" }
										td { +totalFiles.toString() }
									}
									tr {
										th { +"Local files" }
										td { +localFiles.toString() }
									}
								}
							}
						}
					}
				}
			}
		}
}
