package site.remlit.aster.model.config

@Suppress("MagicNumber")
class ConfigurationNote : ConfigurationObject {
	val maxLength = ConfigurationMaxLength()
	val maxAttachments: Int get() = config?.propertyOrNull("note.maxAttachments")?.getString()?.toInt() ?: 10
}
