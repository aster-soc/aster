package site.remlit.mfmkt.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class MfmMention(
	val username: String,
	val host: String? = null,
) : MfmNode {
	override fun toString(): String =
		if (host != null) "@$username@$host" else "@$username"

	override fun equals(other: Any?): Boolean =
		this.toString() == other.toString()

	override fun hashCode(): Int {
		var result = username.hashCode()
		result = 31 * result + (host?.hashCode() ?: 0)
		return result
	}
}
