package site.remlit.aster.model.ap.tag

import kotlinx.serialization.Serializable
import site.remlit.aster.model.ap.ApTag
import site.remlit.aster.model.ap.ApType

@Serializable
data class ApMentionTag(
	override val type: ApType.Tag = ApType.Tag.Mention,
	val href: String,
	val name: String,
) : ApTag()
