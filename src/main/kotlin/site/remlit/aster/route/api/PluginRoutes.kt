package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.registry.PluginRegistry
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.util.authentication

object PluginRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Admin,
			) {
				post("/api/plugins/reload") {
					PluginRegistry.reloadAll()
					call.respond(HttpStatusCode.OK)
				}
			}
		}
}
