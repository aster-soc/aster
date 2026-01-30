package site.remlit.aster.route

import io.ktor.http.ContentType
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.routingRoot
import site.remlit.aster.model.PackageInformation
import site.remlit.aster.registry.RouteRegistry

object DocumentationRoutes {
	fun register() = RouteRegistry.registerRoute {
		swaggerUI("/swagger") {
			info = OpenApiInfo(PackageInformation.name, PackageInformation.version)
			source = OpenApiDocSource.Routing(ContentType.Application.Json) {
				routingRoot.descendants()
			}
		}
	}
}
