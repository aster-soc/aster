package site.remlit.aster.model.ap

import kotlinx.serialization.Serializable

@Serializable
data class ApTombstone(
	val id: String,
	val type: ApType.Object = ApType.Object.Tombstone,
)
