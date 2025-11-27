package site.remlit.aster.route.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.util.authentication

internal object ReportRoutes {
    fun register() =
        RouteRegistry.registerRoute {
            authentication(
                required = true,
                role = RoleType.Mod
            ) {
                get("/api/mod/reports") {
                    call.respond(HttpStatusCode.NotImplemented)
                }

                get("/api/mod/report/{id}") {
                    call.respond(HttpStatusCode.NotImplemented)
                }

                // Edit note on report
                post("/api/mod/report/{id}") {
                    call.respond(HttpStatusCode.NotImplemented)
                }

                post("/api/mod/report/{id}/resolve") {
                    call.respond(HttpStatusCode.NotImplemented)
                }
            }
        }
}
