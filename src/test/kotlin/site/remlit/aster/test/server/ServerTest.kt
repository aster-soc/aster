package site.remlit.aster.test.server

import io.ktor.server.testing.*
import org.junit.jupiter.api.Order
import org.slf4j.LoggerFactory
import site.remlit.aster.module
import site.remlit.aster.service.MigrationService
import site.remlit.aster.test.util.TestDatabaseService
import kotlin.test.Test

@Order(1)
class ServerTest {
	private val logger = LoggerFactory.getLogger(ServerTest::class.java)

	init {
		logger.info("Clearing database...")
		TestDatabaseService.clearDatabase()
		logger.info("Repopulating database...")
		MigrationService.execute()
	}

	@Test
	fun start() = testApplication {
		application {
			module()
		}
	}
}
