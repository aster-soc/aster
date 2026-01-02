package site.remlit.aster.model.ap.activity

import kotlinx.serialization.Serializable
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApObjectWithContext
import site.remlit.aster.model.ap.ApType
import site.remlit.aster.util.serialization.NestedApObjectSerializer

@Serializable
data class ApUpdateActivity(
	val id: String,
	val type: ApType.Activity = ApType.Activity.Update,

	val actor: String,
	@Serializable(with = NestedApObjectSerializer::class)
	val `object`: ApIdOrObject,

	val to: List<String> = emptyList(),
	val cc: List<String> = emptyList()
) : ApObjectWithContext()
