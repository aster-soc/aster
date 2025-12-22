package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@Serializable
@JsExport
data class Emoji(
	val id: String,
	val apId: String,

	val name: String,
	val category: String? = null,
	val host: String? = null,
	val src: String,

	val createdAt: Instant,
	val updatedAt: Instant? = null,
)
