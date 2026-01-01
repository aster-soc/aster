package site.remlit.aster.model.ap

import kotlinx.serialization.Serializable

@Serializable
data class ApOrderedCollectionPage(
	val id: String,
	val partOf: String? = null,
	val orderedItems: List<String> = emptyList(),
	val totalItems: Int? = orderedItems.size,
)
