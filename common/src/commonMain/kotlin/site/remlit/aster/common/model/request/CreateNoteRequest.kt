package site.remlit.aster.common.model.request

import kotlinx.serialization.Serializable
import site.remlit.aster.common.model.NoteAttachment
import kotlin.js.JsExport

@Serializable
@JsExport
data class CreateNoteRequest(
	val cw: String? = null,
	val content: String? = null,
	val visibility: String,
	val replyingTo: String? = null,
	val attachments: List<String> = emptyList(),
)
