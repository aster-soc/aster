package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.remlit.aster.model.ApiException
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.SearchService
import site.remlit.aster.util.authentication

object SearchRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true
			) {
				get("/api/search") {
					val query = call.request.queryParameters["q"]

					if (query == null || query.isBlank())
						throw ApiException(HttpStatusCode.BadRequest, "Query missing")

					call.respond(SearchService.search(query))
				}
			}
		}
}
