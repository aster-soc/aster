package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class RelationshipPair(
	val to: Relationship? = null,
	val from: Relationship? = null
)
