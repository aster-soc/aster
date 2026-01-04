package site.remlit.aster.common.model.streaming

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
enum class StreamingMessageType {
	Auth,
	Subscribe,
	Unsubscribe,
	StreamEvent,
	Echo
}
