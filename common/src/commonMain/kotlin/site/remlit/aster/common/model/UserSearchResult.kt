package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class UserSearchResult(
	val user: User,
) : SearchResult
