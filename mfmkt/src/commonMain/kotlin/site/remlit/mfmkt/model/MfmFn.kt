package site.remlit.mfmkt.model

data class MfmFn(
	val function: String,
	val props: Map<String, Any?> = emptyMap(),
	val children: List<MfmNode> = emptyList(),
) : MfmNode
