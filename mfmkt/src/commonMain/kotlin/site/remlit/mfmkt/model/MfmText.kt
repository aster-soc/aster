package site.remlit.mfmkt.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class MfmText(
	val text: String
) : MfmNode {
	override fun toString(): String = text

	override fun equals(other: Any?): Boolean =
		this.toString() == other.toString()

	override fun hashCode(): Int =
		text.hashCode()
}
