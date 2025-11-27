package site.remlit.aster.common.model


import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class SearchResults(
	val redirect: Boolean,
	val results: List<SearchResult>
)
