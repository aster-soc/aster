package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class NoteSearchResult(
	val note: Note,
) : SearchResult
