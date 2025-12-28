package site.remlit.aster.model

import kotlinx.serialization.Serializable

@Serializable
data class NodeInfo(
	val version: String,
	val software: NodeInfoSoftware,
	val protocols: List<String> = listOf("activitypub"),
	val openRegistrations: Boolean,
	val usage: NodeInfoUsage,
    val metadata: NodeInfoMetadata
)
