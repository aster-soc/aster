package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@JsExport
@Serializable
data class Instance(
	val id: String,
	val host: String,

	val name: String?,
	val description: String?,
	val color: String?,
	val icon: String?,

	val software: String?,
	val version: String?,
	val contact: String?,

	val createdAt: Instant,
	val updatedAt: Instant? = null,
)
