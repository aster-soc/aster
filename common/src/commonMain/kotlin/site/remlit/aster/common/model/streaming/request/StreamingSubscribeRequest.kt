package site.remlit.aster.common.model.streaming.request

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import site.remlit.aster.common.model.streaming.StreamingMessage
import site.remlit.aster.common.model.streaming.StreamingMessageType

@JsExport
@Serializable
data class StreamingSubscribeRequest(
	override val type: StreamingMessageType = StreamingMessageType.Subscribe,
	val stream: String,
) : StreamingMessage
