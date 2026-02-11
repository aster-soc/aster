package site.remlit.aster.model.config

data class ConfigurationSpecificQueue(
	val concurrency: Int,
	val maxRetries: Int
) : ConfigurationObject
