package site.remlit.aster.common.model.streaming

import kotlin.js.JsExport

@JsExport
interface StreamingMessage {
	val type: StreamingMessageType
}
