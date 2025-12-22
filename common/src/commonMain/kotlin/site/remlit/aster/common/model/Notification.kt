package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@JsExport
@Serializable
data class Notification(
	val id: String,
	val type: site.remlit.aster.common.model.type.NotificationType,

	val to: User,
	val from: User,

	val note: Note?,
	val relationship: Relationship?,

	val createdAt: Instant
)
