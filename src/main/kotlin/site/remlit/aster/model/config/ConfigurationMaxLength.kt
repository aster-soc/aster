package site.remlit.aster.model.config

@Suppress("MagicNumber")
class ConfigurationMaxLength: ConfigurationObject {
	val cw: Int get() = config?.propertyOrNull("configuration.maxCwLength")?.getString()?.toInt() ?: 1024
	val content: Int get() = config?.propertyOrNull("configuration.maxLength.content")?.getString()?.toInt() ?: 8192
}
