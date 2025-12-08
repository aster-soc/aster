package site.remlit.mfmkt.model

data class MfmText(
	val text: String
) : MfmNode {
	override fun toString(): String = text

	override fun equals(other: Any?): Boolean =
		this.toString() == other.toString()

	override fun hashCode(): Int =
		text.hashCode()
}
