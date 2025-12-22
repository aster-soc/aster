package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@Serializable
@JsExport
data class Report(
	val id: String,
	val sender: User,
	val comment: String? = null,
	val user: User? = null,
	val note: Note? = null,
	val resolvedBy: User? = null,
	val createdAt: Instant,
	val updatedAt: Instant? = null,
)
