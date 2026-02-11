package site.remlit.aster.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import site.remlit.aster.model.Configuration

object Database {
	val config = HikariConfig().apply {
		jdbcUrl =
			"jdbc:postgresql://${Configuration.database.host}:${Configuration.database.port}/${Configuration.database.db}"
		username = Configuration.database.user
		password = Configuration.database.password
	}

	val dataSource = HikariDataSource(config)

	val connection = Database.connect(dataSource)
}
