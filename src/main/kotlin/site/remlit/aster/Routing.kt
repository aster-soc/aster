package site.remlit.aster

import io.ktor.openapi.OpenApiDoc
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.plus
import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.model.PackageInformation
import site.remlit.aster.registry.RouteRegistry
import kotlin.time.Duration.Companion.seconds

@ApiStatus.Internal
internal fun Application.configureRouting() {
	RouteRegistry.registerInternal()

	install(RateLimit) {
		global {
			rateLimiter(limit = 500, refillPeriod = 5.seconds)
		}
	}

	routing {
		RouteRegistry.installRoutes(this)
	}
}
