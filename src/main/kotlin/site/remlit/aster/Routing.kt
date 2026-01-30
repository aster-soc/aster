package site.remlit.aster

import io.ktor.openapi.OpenApiDoc
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.plus
import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.model.PackageInformation
import site.remlit.aster.registry.RouteRegistry

@ApiStatus.Internal
internal fun Application.configureRouting() {
	RouteRegistry.registerInternal()

	routing {
		RouteRegistry.installRoutes(this)
	}
}
