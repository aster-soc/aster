package site.remlit.aster.model.ap

import kotlinx.serialization.Serializable

@Serializable
data class ApDocument(
	val type: ApType.Object = ApType.Object.Document,
	val url: String,
	val mediaType: String,

	val name: String? = null,
	val summary: String? = name,

	val sensitive: Boolean = false,
) : ApObject
