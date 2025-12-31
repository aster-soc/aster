package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@JsExport
@Serializable
data class Relationship(
	val id: String,

	val type: site.remlit.aster.common.model.type.RelationshipType,

	val to: User,
	val from: User,

	val pending: Boolean,
	val activityId: String? = null,

	val createdAt: Instant,
	val updatedAt: Instant? = null
)
