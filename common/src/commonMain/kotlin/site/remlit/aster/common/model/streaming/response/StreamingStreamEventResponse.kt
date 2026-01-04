package site.remlit.aster.common.model.streaming.response

import kotlinx.serialization.Serializable
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.Notification
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.streaming.StreamingMessage
import site.remlit.aster.common.model.streaming.StreamingMessageType
import kotlin.js.JsExport

@JsExport
@Serializable
data class StreamingStreamEventResponse(
	override val type: StreamingMessageType = StreamingMessageType.StreamEvent,
	val stream: String,

	val user: User? = null,
	val note: Note? = null,
	val notification: Notification? = null,
) : StreamingMessage
