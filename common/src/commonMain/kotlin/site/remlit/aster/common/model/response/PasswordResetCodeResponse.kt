package site.remlit.aster.common.model.response

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class PasswordResetCodeResponse(
	val code: String,
)
