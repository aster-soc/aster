package site.remlit.aster.model.ap.activity

import kotlinx.serialization.Serializable
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApObjectWithContext
import site.remlit.aster.model.ap.ApType
import site.remlit.aster.util.serialization.NestedApObjectSerializer

@Serializable
data class ApDeleteActivity(
	val id: String,
	val type: ApType.Activity = ApType.Activity.Delete,

	val actor: String? = null,
	@Serializable(with = NestedApObjectSerializer::class)
	val `object`: ApIdOrObject,

	val to: List<String>,
	val cc: List<String>
) : ApObjectWithContext()
