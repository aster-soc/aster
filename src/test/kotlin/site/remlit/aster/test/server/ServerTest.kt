package site.remlit.aster.test.server

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.BeforeClass
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.common.model.response.AuthResponse
import site.remlit.aster.service.AuthService
import site.remlit.aster.service.MigrationService
import site.remlit.aster.service.RandomService
import site.remlit.aster.service.SetupService
import site.remlit.aster.service.UserService
import site.remlit.aster.test.util.TestDatabaseService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ServerTest {
	companion object {
		private val logger = LoggerFactory.getLogger(ServerTest::class.java)

		@BeforeClass
		@JvmStatic
		fun setup() = runTest {
			logger.info("Clearing database...")
			TestDatabaseService.clearDatabase()
			logger.info("Repopulating database...")
			MigrationService.execute()
			logger.info("Setting up instance...")
			SetupService.setup()
		}
	}

	@Test
	fun `auth route test`() = setupTestApplication {
		val register = client.post("/api/register") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("username", "kodee")
				put("password", "password")
			})
		}

		assertEquals(HttpStatusCode.OK, register.status)

		val registerResponse = register.body<AuthResponse>()

		assertNotNull(registerResponse.token)
		assertEquals("kodee", registerResponse.user.username)
		assertNull(registerResponse.user.host)
		assertEquals(true, registerResponse.user.activated)

		/* Login */

		val login = client.post("/api/login") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("username", "kodee")
				put("password", "password")
			})
		}

		assertEquals(HttpStatusCode.OK, login.status)

		val loginResponse = login.body<AuthResponse>()

		assertNotNull(loginResponse.token)

		assertEquals("kodee", loginResponse.user.username)
		assertNull(loginResponse.user.host)
		assertEquals(true, loginResponse.user.activated)
	}

	@Test
	fun `get and lookup user route test`() = setupTestApplication {
		val instanceActor = UserService.getInstanceActor()

		val user = client.get("/api/user/${instanceActor.id}")

		assertEquals(HttpStatusCode.OK, user.status)

		val userResponse = user.body<User>()

		assertEquals(instanceActor.id.toString(), userResponse.id)

		/* Lookup */

		val lookupUser = client.get("/api/lookup/@${instanceActor.username}")

		assertEquals(HttpStatusCode.OK, lookupUser.status)

		val lookupUserResponse = lookupUser.body<User>()

		assertEquals(instanceActor.id.toString(), lookupUserResponse.id)
		assertEquals(userResponse, lookupUserResponse) // Should be exactly the same
	}

	@Test
	fun `create note route test`() = setupTestApplication {
		val instanceActor = UserService.getInstanceActor()

		val token = AuthService.registerToken(instanceActor)

		val testNoteContent = RandomService.generateString()

		val createNote = client.post("/api/note") {
			contentType(ContentType.Application.Json)
			headers { append("Authorization", "Bearer $token") }
			setBody(buildJsonObject {
				put("content", testNoteContent)
				put("visibility", "public")
			})
		}

		assertEquals(HttpStatusCode.OK, createNote.status)

		val createNoteResponse = createNote.body<Note>()

		assertEquals(testNoteContent, createNoteResponse.content)
		assertEquals(Visibility.Public, createNoteResponse.visibility)
		assertEquals(instanceActor.id.toString(), createNoteResponse.user.id)
	}
}
