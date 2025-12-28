package site.remlit.aster.model

import kotlinx.serialization.Serializable

@Serializable
data class NodeInfoMetadata(
    val nodeName: String,
    val nodeDescription: String? = null,
    val maintainer: NodeInfoMetadataMaintainer? = null,
    val repositoryUrl: String,
    val feedbackUrl: String,
    val themeColor: String,
)
