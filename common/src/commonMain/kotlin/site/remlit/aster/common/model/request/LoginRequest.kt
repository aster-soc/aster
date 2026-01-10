package site.remlit.aster.common.model.request

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class LoginRequest(
	val username: String,
	val password: String,
	val totp: Int? = null,
)
