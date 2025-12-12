package site.remlit.aster.test.server

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import site.remlit.aster.module
import site.remlit.aster.util.jsonConfig

inline fun setupTestApplication(
	crossinline block: suspend ApplicationTestBuilder.() -> Unit
) = testApplication {
	application {
		module()
	}

	client = createClient {
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

	block()
}
