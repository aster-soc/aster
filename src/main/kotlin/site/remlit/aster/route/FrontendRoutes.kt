package site.remlit.aster.route

import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry

internal object FrontendRoutes {
	fun register() {
		RouteRegistry.registerRoute {
			staticResources("/uikit", "uikit")

			get("/favicon.ico") {
				call.respondRedirect("/uikit/branding/favicon.ico")
			}

			if (Configuration.builtinFrontend)
				singlePageApplication {
					useResources = true
					filesPath = "frontend"
				}
		}
	}
}
