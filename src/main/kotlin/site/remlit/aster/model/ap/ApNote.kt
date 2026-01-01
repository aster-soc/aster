package site.remlit.aster.model.ap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.service.ap.ApVisibilityService
import site.remlit.aster.util.model.fromEntity
import kotlin.time.Instant

@Serializable
data class ApNote(
	val id: String,
	val type: ApType.Object = ApType.Object.Note,

	val attributedTo: String,
	val inReplyTo: String? = null,

	val summary: String? = null,
	@SerialName("_misskey_summary")
	val misskeySummary: String? = null,

	val content: String? = null,
	@SerialName("_misskey_content")
	val misskeyContent: String? = null,

	val sensitive: Boolean = summary.isNullOrBlank(),

	val attachment: List<ApDocument> = emptyList(),

	val replies: ApIdOrObject? = null,

	val published: Instant,
	val visibility: Visibility? = null,

	val to: List<String>,
	val cc: List<String>

) : ApObjectWithContext() {
	companion object {
		fun fromEntity(note: Note): ApNote {
			val (to, cc) = ApVisibilityService.visibilityToCc(
				note.visibility,
				followersUrl = null,
				to = note.to
			)

			return ApNote(
				id = note.apId,
				attributedTo = note.user.apId,
				inReplyTo = note.replyingTo?.apId,
				content = note.content,
				misskeyContent = note.content,
				attachment = note.attachments.map {
					ApDocument(url = it.src, mediaType = it.type, name = it.alt)
				},
				published = note.createdAt,
				visibility = note.visibility,
				to = to,
				cc = cc
			)
		}
	}
}
