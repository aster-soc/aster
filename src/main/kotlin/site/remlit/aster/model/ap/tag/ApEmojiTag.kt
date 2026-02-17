package site.remlit.aster.model.ap.tag

import kotlinx.serialization.Serializable
import site.remlit.aster.model.ap.ApImage
import site.remlit.aster.model.ap.ApTag
import site.remlit.aster.model.ap.ApType

@Serializable
data class ApEmojiTag(
	val id: String,
	override val type: ApType.Tag = ApType.Tag.Emoji,
	val name: String,
	val icon: ApImage,
) : ApTag()
