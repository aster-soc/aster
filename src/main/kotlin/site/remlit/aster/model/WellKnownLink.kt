package site.remlit.aster.model

import kotlinx.serialization.Serializable

@Serializable
data class WellKnownLink(
	val rel: String,
	val href: String? = null,
	val type: String? = null,
	val template: String? = null,
)
