package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class NoteWithReplies(
	val note: Note,
	val replies: List<NoteWithReplies>,
)
