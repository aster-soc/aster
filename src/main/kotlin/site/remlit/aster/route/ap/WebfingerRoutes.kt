package site.remlit.aster.route.ap

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.WellKnown
import site.remlit.aster.model.WellKnownLink
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.UserService

internal object WebfingerRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			get("/.well-known/webfinger") {
				val resource = call.queryParameters.getOrFail("resource")

				if (!resource.startsWith("acct:") || resource == "acct:")
					throw ApiException(HttpStatusCode.BadRequest, "Only 'acct' resource type supported")

				val username = resource
					.replace("acct:@", "")
					.replace("acct:", "")
					.replace("@${Configuration.url.host}", "")
					.replace("@", "")

				val user = UserService.get(
					UserTable.username eq username
							and (UserTable.host eq null)
				)

				if (user == null || user.suspended || !user.activated)
					throw ApiException(HttpStatusCode.NotFound)

				call.response.headers.append("Content-Type", "application/jrd+json")

				call.respond(
					status = HttpStatusCode.OK,
					message = WellKnown(
						subject = "${user.username}@${Configuration.url.host}",
						aliases = listOf(
							user.apId,
							Configuration.url.toString() + "@" + user.username,
						),
						links = listOf(
							WellKnownLink(
								rel = "self",
								type = "application/activity+json",
								href = user.apId,
							),
							WellKnownLink(
								rel = "self",
								type = "application/ld+json; profile\"https://www.w3.org/ns/activitystreams\"",
								href = user.apId,
							),
							WellKnownLink(
								rel = "http://webfinger.net/rel/profile-page",
								type = "text/html",
								href = Configuration.url.toString() + "@" + user.username,
							)
						)
					)
				)
			}
		}
}
