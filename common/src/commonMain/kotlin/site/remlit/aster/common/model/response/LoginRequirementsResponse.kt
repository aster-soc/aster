package site.remlit.aster.common.model.response

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class LoginRequirementsResponse(
	val totp: Boolean
)
