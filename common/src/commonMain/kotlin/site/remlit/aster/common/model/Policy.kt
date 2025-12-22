package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@JsExport
@Serializable
data class Policy(
	val id: String,

	val type: site.remlit.aster.common.model.type.PolicyType,

	val host: String,
	val content: String? = null,

	val createdAt: Instant,
	val updatedAt: Instant? = null
)
