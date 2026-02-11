package site.remlit.aster.model.config

import site.remlit.aster.exception.ConfigurationException

@Suppress("MagicNumber")
class ConfigurationDatabase : ConfigurationObject {
	val host: String get() = config?.propertyOrNull("database.host")?.getString() ?: "127.0.0.1"
	val port: String get() = config?.propertyOrNull("database.port")?.getString() ?: "5432"
	val db: String
		get() = config?.propertyOrNull("database.db")?.getString()
			?: throw ConfigurationException("Configuration is missing 'database.db' attribute.")
	val user: String
		get() = config?.propertyOrNull("database.user")?.getString()
			?: throw ConfigurationException("Configuration is missing 'database.user' attribute.")
	val password: String
		get() = config?.propertyOrNull("database.password")?.getString()
			?: throw ConfigurationException("Configuration is missing 'database.password' attribute.")
}
