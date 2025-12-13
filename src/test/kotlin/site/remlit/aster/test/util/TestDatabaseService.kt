package site.remlit.aster.test.util

import site.remlit.aster.db.Database

object TestDatabaseService {
	private val dataSource = Database.dataSource

	fun clearDatabase() {
		dataSource.connection.use { conn ->
			conn.createStatement().use { stmt ->
				stmt.execute("DROP SCHEMA public CASCADE;")
				stmt.execute("CREATE SCHEMA public;")
				stmt.execute("GRANT ALL ON SCHEMA public TO postgres;")
				stmt.execute("GRANT ALL ON SCHEMA public TO public;")
			}
		}
	}
}
