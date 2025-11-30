package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.neq
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.db.table.ReportTable
import site.remlit.aster.model.ApiException
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.ReportService
import site.remlit.aster.service.TimelineService
import site.remlit.aster.util.authenticatedUserKey
import site.remlit.aster.util.authentication

internal object ReportRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Mod
			) {
				get("/api/mod/reports") {
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())

					val reports = ReportService.getMany(
						where = ReportTable.id neq null and
								(ReportTable.createdAt less since),
						take = take
					)

					if (reports.isEmpty())
						return@get call.respond(HttpStatusCode.NoContent)

					call.respond(reports)
				}

				get("/api/mod/report/{id}") {
					val report = ReportService.getById(call.parameters.getOrFail("id"))
						?: throw ApiException(HttpStatusCode.NotFound, "Report not found")

					call.respond(report)
				}

				post("/api/mod/report/{id}/resolve") {
					val user = call.attributes[authenticatedUserKey]
					val report = ReportService.getById(call.parameters.getOrFail("id"))
						?: throw ApiException(HttpStatusCode.NotFound, "Report not found")

					val resolved = ReportService.resolve(user, report.id)

					call.respond(resolved)
				}
			}
		}
}
