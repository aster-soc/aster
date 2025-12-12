package site.remlit.aster.test.server

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.PackageInformation
import site.remlit.aster.util.jsonConfig

open class RoutingTest {
	fun createClient(): HttpClient {
		return HttpClient(CIO) {
			defaultRequest {
				headers.append(
					"User-Agent",
					"${PackageInformation.name}-test/${PackageInformation.version} (+${Configuration.url})"
				)
			}

			install(ContentNegotiation) {
				json(jsonConfig)

				register(
					ContentType.parse("application/ld+json"),
					KotlinxSerializationConverter(jsonConfig)
				)
				register(
					ContentType.parse("application/activity+json"),
					KotlinxSerializationConverter(jsonConfig)
				)
				register(
					ContentType.parse("application/jrd+json"),
					KotlinxSerializationConverter(jsonConfig)
				)
			}
		}
	}
}
