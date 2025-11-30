package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class Report(
	val id: String,
	val comment: String? = null,
	val user: User? = null,
	val note: Note? = null,
	val resolvedBy: User? = null
)
