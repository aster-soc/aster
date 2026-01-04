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
		fun parse(string: String): Array<MfmNode> {
			val nodes = mutableListOf<MfmNode>()

			var last: String? = null
			var next: String? = null

			for (mention in extractMentions(string)) {
				val split = string.split(mention.toString())

				if (last == null) {
					val zero = split.getOrNull(0) ?: continue
					last = zero + mention.toString()
					nodes.add(MfmText(zero))
					nodes.add(mention)
				} else {
					val zero = split.getOrNull(0) ?: continue
					val new = zero.split(last).getOrNull(1) ?: continue
					nodes.add(MfmText(new))
					last = new
					nodes.add(mention)
					next = split.getOrNull(1) ?: continue
				}
			}

			if (next != null || nodes.isEmpty())
				nodes.add(MfmText(next ?: string))

			return nodes.toTypedArray()
		}

		private fun extractMentions(string: String): List<MfmMention> =
			Regex("@([a-zA-Z._-]+)(@[a-zA-Z._-]+)?").findAll(string).map {
				val split = it.value.split("@")
				MfmMention(split[1], split.getOrNull(2))
			}.toList()
	}
}
