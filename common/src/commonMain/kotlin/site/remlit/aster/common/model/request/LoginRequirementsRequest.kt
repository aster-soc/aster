package site.remlit.aster.common.model.request

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class LoginRequirementsRequest(
	val username: String,
)
