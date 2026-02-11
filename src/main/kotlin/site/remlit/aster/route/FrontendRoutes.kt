package site.remlit.aster.route

import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.remlit.aster.model.config.Configuration
import site.remlit.aster.registry.RouteRegistry

internal object FrontendRoutes {
	fun register() {
		RouteRegistry.registerRoute {
			staticResources("/uikit", "uikit")

			get("/robots.txt") {
				call.respondText {
					"User-agent: *\n" + "Disallow: /"
				}
			}

			get("/favicon.ico") {
				call.respondRedirect("/uikit/branding/favicon.ico")
			}

			get("/apple-touch-icon.png") {
				call.respondRedirect("/uikit/branding/apple-touch-icon.png")
			}

			get("/apple-touch-icon-120x120.png") {
				call.respondRedirect("/uikit/branding/apple-touch-icon-120x120.png")
			}

			if (Configuration.builtinFrontend)
				singlePageApplication {
					useResources = true
					filesPath = "frontend"
				}
		}
	}
}
