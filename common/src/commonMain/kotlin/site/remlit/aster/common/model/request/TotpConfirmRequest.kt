package site.remlit.aster.common.model.request

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class TotpConfirmRequest(
	val code: Int,
)
