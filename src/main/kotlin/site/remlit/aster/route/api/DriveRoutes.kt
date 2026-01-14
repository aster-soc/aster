package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import site.remlit.aster.db.table.DriveFileTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.ApiException
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.DriveService
import site.remlit.aster.service.TimelineService
import site.remlit.aster.util.authenticatedUserKey
import site.remlit.aster.util.authentication

internal object DriveRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true
			) {
				get("/api/drive") {
					val authenticatedUser = call.attributes[authenticatedUserKey]
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())

					val files = DriveService.getMany(
						where = UserTable.id eq authenticatedUser.id.toString()
								and (DriveFileTable.createdAt less since),
						take = take
					)

					if (files.isEmpty())
						return@get call.respond(HttpStatusCode.NoContent)

					call.respond(files)
				}

				get("/api/drive/file/{id}") {
					val authenticatedUser = call.attributes[authenticatedUserKey]

					val file = DriveService.getById(call.parameters.getOrFail("id"))

					if (
						file == null ||
						file.user.id != authenticatedUser.id.toString()
						) throw ApiException(HttpStatusCode.NotFound, "File not found")

					call.respond(file)
				}

				delete("/api/drive/file/{id}") {
					val authenticatedUser = call.attributes[authenticatedUserKey]

					val file = DriveService.getById(call.parameters.getOrFail("id"))

					if (
						file == null ||
						file.user.id != authenticatedUser.id.toString()
						) throw ApiException(HttpStatusCode.NotFound, "File not found")

					DriveService.deleteById(file.id)

					call.respond(HttpStatusCode.OK)
				}

				post("/api/drive/file/{id}") {
					call.respond(HttpStatusCode.NotImplemented)
				}
			}
		}
}
