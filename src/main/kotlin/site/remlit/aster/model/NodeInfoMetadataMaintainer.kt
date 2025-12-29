package site.remlit.aster.model

import kotlinx.serialization.Serializable

@Serializable
data class NodeInfoMetadataMaintainer(
    val name: String,
    val email: String,
)
