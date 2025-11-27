package site.remlit.aster.route.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.util.authentication

internal object EmojiRoutes {
    fun register() =
        RouteRegistry.registerRoute {
            get("/api/emojis") {
                call.respond(HttpStatusCode.NotImplemented)
            }

            get("/api/emoji/{id}") {
                call.respond(HttpStatusCode.NotImplemented)
            }

            authentication(
                required = true,
                role = RoleType.Mod
            ) {
                post("/api/emoji/{id}") {
                    call.respond(HttpStatusCode.NotImplemented)
                }

                post("/api/emoji") {
                    call.respond(HttpStatusCode.NotImplemented)
                }
            }
        }
}
