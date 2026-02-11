package site.remlit.aster.route.admin

import io.ktor.http.HttpStatusCode
import io.ktor.server.html.respondHtml
import io.ktor.server.routing.get
import io.ktor.server.routing.path
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.neq
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.db.table.PolicyTable
import site.remlit.aster.model.config.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.PolicyService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.webcomponent.adminButton
import site.remlit.aster.util.webcomponent.adminInput
import site.remlit.aster.util.webcomponent.adminListNav
import site.remlit.aster.util.webcomponent.adminPage
import site.remlit.aster.util.webcomponent.ctn

object AdminPolicyRoutes {
	fun register() = RouteRegistry.registerRoute {
		authentication(
			required = true,
			role = RoleType.Mod
		) {
			get("/admin/policies") {
				val take = Configuration.timeline.defaultObjects
				val offset = call.parameters["offset"]?.toLong() ?: 0
				val query = call.request.queryParameters["q"]

				val policies = PolicyService.getMany(
					if (query != null)
						PolicyTable.host like "%$query%"
					else PolicyTable.id neq "",
					take,
					offset
				)

				val totalPolicies = PolicyService.count(
					PolicyTable.id neq ""
				)

				call.respondHtml(HttpStatusCode.OK) {
					adminPage(call.route.path) {
						ctn {
							adminButton("/admin/policies/add") { +"Add policy" }
						}

						table {
							tr {
								classes = setOf("header")
								th { +"Host" }
								th { +"Type" }
								th { +"Content" }
								th { +"Actions" }
							}
							for (policy in policies) {
								tr {
									td { +policy.host }
									td { +policy.type.name }
									td { +(policy.content ?: "No content") }
									td {
										adminButton({ "" }) { +"Delete" }
									}
								}
							}
						}

						p {
							+"${policies.size} policies shown, $totalPolicies total."
						}
						adminListNav(offset, take, query)
					}

				}

				get("/admin/policies/add") {
					call.respondHtml(HttpStatusCode.OK) {
						adminPage(call.route.path) {
							form {
								h2 { +"Add policy" }
								adminInput(InputType.text, "host", "", "example.com", "")
								select {
									option {
										+"Block"
										value = "block"
									}
									option {
										+"Silence"
										value = "silence"
									}
									option {
										+"Force content warning on notes"
										value = "forceContentWarning"
									}
									option {
										+"Force users to be marked sensitive"
										value = "forceSensitive"
									}
								}
								adminInput(InputType.text, "content", "", "Policy content", "")
								adminButton({ "" }) { +"Submit" }
							}
						}
					}
				}
			}
		}
	}
}
