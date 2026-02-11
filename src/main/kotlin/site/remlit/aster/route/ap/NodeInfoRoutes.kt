package site.remlit.aster.route.ap

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import site.remlit.aster.common.model.type.InstanceRegistrationsType
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.config.Configuration
import site.remlit.aster.model.NodeInfo
import site.remlit.aster.model.NodeInfoMetadata
import site.remlit.aster.model.NodeInfoMetadataMaintainer
import site.remlit.aster.model.NodeInfoSoftware
import site.remlit.aster.model.NodeInfoUsage
import site.remlit.aster.model.NodeInfoUsageUsers
import site.remlit.aster.model.PackageInformation
import site.remlit.aster.model.WellKnown
import site.remlit.aster.model.WellKnownLink
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.UserService

internal object NodeInfoRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			get("/.well-known/nodeinfo") {
				call.response.headers.append("Content-Type", "application/jrd+json")

				call.respond(
					status = HttpStatusCode.OK,
					message = WellKnown(
						links = listOf(
							WellKnownLink(
								rel = "http://nodeinfo.diaspora.software/ns/schema/2.1",
								href = Configuration.url.toString() + "nodeinfo/2.1",
							),
							WellKnownLink(
								rel = "http://nodeinfo.diaspora.software/ns/schema/2.0",
								href = Configuration.url.toString() + "nodeinfo/2.0",
							)
						)
					)
				)
			}

			get("/nodeinfo/{version}") {
				val version = call.parameters.getOrFail("version")

				if (version != "2.0" && version != "2.1")
					throw ApiException(HttpStatusCode.BadRequest, "Invalid version")

				val userCount = UserService.count(
					UserTable.host eq null
						and (UserTable.username neq "instance.actor")
				).toInt()

				val noteCount = NoteService.count(
					UserTable.host eq null
						and (UserTable.username neq "instance.actor")
				).toInt()

				val nodeInfo =
					NodeInfo(
						version = version,
						software = NodeInfoSoftware(
							name = PackageInformation.name,
							version = PackageInformation.version,
						),
						protocols = listOf("activitypub"),
						openRegistrations = Configuration.registrations == InstanceRegistrationsType.Open,
						usage = NodeInfoUsage(
							users = NodeInfoUsageUsers(userCount),
							localPosts = noteCount,
						),
						metadata = NodeInfoMetadata(
							nodeName = Configuration.name,
							nodeDescription = Configuration.description,
							maintainer = NodeInfoMetadataMaintainer(
								Configuration.maintainer.name,
								Configuration.maintainer.email
							),
							repositoryUrl = PackageInformation.repo,
							feedbackUrl = PackageInformation.bugTracker,
							themeColor = Configuration.color,
						)
					)

				call.respond(nodeInfo)
			}
		}
}
