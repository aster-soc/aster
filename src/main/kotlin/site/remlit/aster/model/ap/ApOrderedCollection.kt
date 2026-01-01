package site.remlit.aster.model.ap

import kotlinx.serialization.Serializable

@Serializable
data class ApOrderedCollection(
	val type: ApType.Object = ApType.Object.OrderedCollection,
	val orderedItems: List<String> = emptyList(),
	val first: String? = null,
	val totalItems: Int = orderedItems.size
) : ApObjectWithContext()
