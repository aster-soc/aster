package site.remlit.aster.test.server

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import site.remlit.aster.common.model.response.AuthResponse
import site.remlit.aster.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthRouteTest : RoutingTest() {
	@Test
	fun `register routes`() = testApplication {
		application {
			module()
		}

		val client = createClient()

		val register = client.post("/api/register") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("username", "kodee")
				put("password", "password")
			})
		}

		assertEquals(register.status, HttpStatusCode.OK)

		val registerResponse = register.body<AuthResponse>()

		assertNotNull(registerResponse.token)
		assertEquals(registerResponse.user.username, "kodee")
		assertNull(registerResponse.user.host)
		assertEquals(registerResponse.user.activated, true)
	}

	@Test
	fun `login routes`() = testApplication {
		application {
			module()
		}

		val client = createClient()

		val register = client.post("/api/login") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("username", "kodee")
				put("password", "password")
			})
		}

		assertEquals(register.status, HttpStatusCode.OK)

		val registerResponse = register.body<AuthResponse>()

		assertNotNull(registerResponse.token)
		assertEquals(registerResponse.user.username, "kodee")
		assertNull(registerResponse.user.host)
		assertEquals(registerResponse.user.activated, true)
	}
}
