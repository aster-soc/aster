package site.remlit.aster.model.config

@Suppress("MagicNumber")
class ConfigurationTimeline : ConfigurationObject {
	val defaultObjects: Int get() = config?.propertyOrNull("timeline.defaultObjects")?.getString()?.toIntOrNull() ?: 15
	val maxObjects: Int get() = config?.propertyOrNull("timeline.maxObjects")?.getString()?.toIntOrNull() ?: 35

	val local: ConfigurationSpecificTimeline
		get() = ConfigurationSpecificTimeline(
			authRequired = (config?.propertyOrNull("timeline.local.authRequired")?.getString()?.toBooleanStrictOrNull()
				?: false)
		)
	val bubble: ConfigurationBubbleTimeline
		get() = ConfigurationBubbleTimeline(
			authRequired = (config?.propertyOrNull("timeline.bubble.authRequired")?.getString()?.toBooleanStrictOrNull()
				?: false),
			hosts = (config?.propertyOrNull("timeline.bubble.hosts")?.getList().orEmpty())
		)
	val public: ConfigurationSpecificTimeline
		get() = ConfigurationSpecificTimeline(
			authRequired = (config?.propertyOrNull("timeline.public.authRequired")?.getString()?.toBooleanStrictOrNull()
				?: false)
		)
}
