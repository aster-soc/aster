package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@JsExport
@Serializable
data class DriveFile(
	val id: String,

	val type: String,
	val src: String,
	val alt: String?,
	val blurHash: String?,

	val sensitive: Boolean,

	val user: User,

	val createdAt: Instant,
	val updatedAt: Instant? = null,
)
