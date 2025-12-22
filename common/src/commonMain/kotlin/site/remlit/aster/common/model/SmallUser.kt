package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@JsExport
@Serializable
data class SmallUser(
	val id: String,

	val apId: String,

	val username: String,
	val host: String? = null,
	val displayName: String? = null,

	val avatar: String? = null,
	val avatarAlt: String? = null,

	val automated: Boolean = false,
	val sensitive: Boolean = false,

	val isCat: Boolean = false,

	val createdAt: Instant,
	val updatedAt: Instant? = null
)
