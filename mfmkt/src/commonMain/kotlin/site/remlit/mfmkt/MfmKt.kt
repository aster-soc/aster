package site.remlit.mfmkt

import site.remlit.mfmkt.model.MfmMention
import site.remlit.mfmkt.model.MfmNode
import site.remlit.mfmkt.model.MfmText
import kotlin.js.ExperimentalJsStatic
import kotlin.js.JsExport
import kotlin.js.JsStatic
import kotlin.jvm.JvmStatic

/**
 * Object for handling MFM
 *
 * @since 2025.12.1.0-SNAPSHOT
 * */
@JsExport
@OptIn(ExperimentalJsStatic::class)
@Suppress("UtilityClassWithPublicConstructor")
class MfmKt {
	companion object {
		@JsStatic
		@JvmStatic
		fun parse(string: String): List<MfmNode> {
			val nodes = mutableListOf<MfmNode>()

			var last: String? = null
			var next: String? = null

			for (mention in extractMentions(string)) {
				val split = string.split(mention.toString())

				if (last == null) {
					last = split[0] + mention.toString()
					nodes.add(MfmText(split[0]))
					nodes.add(mention)
				} else {
					val new = split[0].split(last)[1]
					nodes.add(MfmText(new))
					last = new
					nodes.add(mention)
					next = split[1]
				}
			}

			if (next != null) nodes.add(MfmText(next))

			return nodes
		}

		private fun extractMentions(string: String): List<MfmMention> =
			Regex("@([a-zA-Z._-]+)(@[a-zA-Z._-]+)?").findAll(string).map {
				val split = it.value.split("@")
				MfmMention(split[1], split.getOrNull(2))
			}.toList()
	}
}
