package site.remlit.aster.model.config

data class ConfigurationBubbleTimeline(
	val authRequired: Boolean,
	val hosts: List<String>,
) : ConfigurationObject
