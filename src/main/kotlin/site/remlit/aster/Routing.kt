package site.remlit.aster

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.registry.RouteRegistry

@ApiStatus.Internal
internal fun Application.configureRouting() {
	RouteRegistry.registerInternal()

	routing {
		// swaggerUI(path = "swagger", swaggerFile = "openapi.yaml")
		RouteRegistry.installRoutes(this)
	}
}
