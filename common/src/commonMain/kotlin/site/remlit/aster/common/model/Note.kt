package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@JsExport
@Serializable
data class Note(
	val id: String,

	val apId: String,
	val conversation: String? = null,

	val user: User,
	val replyingTo: Note? = null,

	val cw: String? = null,
	val content: String? = null,

	val visibility: Visibility,
	val to: List<String>? = null,
	val tags: List<String>? = null,

	val repeat: Note? = null,

	val createdAt: Instant,
	val updatedAt: Instant? = null,

	val likes: List<SmallUser> = emptyList(),
	val reactions: List<SmallUser> = emptyList(),
	val repeats: List<SmallNote> = emptyList()
)
