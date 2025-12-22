package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@JsExport
@Serializable
data class Invite(
	val id: String,

	val code: String,

	val user: User? = null,
	val creator: User,

	val createdAt: Instant,
	val usedAt: Instant? = null,
)
