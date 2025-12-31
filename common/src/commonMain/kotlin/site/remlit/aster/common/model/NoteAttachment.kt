package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class NoteAttachment(
	val id: String,
	val src: String,
	val alt: String? = null,
	val type: String,
)
