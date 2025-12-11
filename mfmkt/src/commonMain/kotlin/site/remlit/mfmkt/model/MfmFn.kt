package site.remlit.mfmkt.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class MfmFn(
	val function: String,
	val props: Map<String, String?> = emptyMap(),
	val children: List<MfmNode> = emptyList(),
) : MfmNode
