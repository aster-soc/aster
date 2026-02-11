package site.remlit.aster.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.runBlocking
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.common.model.streaming.StreamingMessage
import site.remlit.aster.common.model.streaming.StreamingMessageType
import site.remlit.aster.common.model.streaming.request.StreamingAuthRequest
import site.remlit.aster.common.model.streaming.request.StreamingSubscribeRequest
import site.remlit.aster.common.model.streaming.request.StreamingUnsubscribeRequest
import site.remlit.aster.common.model.streaming.response.StreamingStreamEventResponse
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.event.note.NoteCreateEvent
import site.remlit.aster.event.notification.NotificationCreateEvent
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.AuthService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.util.jsonConfig
import site.remlit.effekt.effect

object StreamingRoutes {
	fun register() = RouteRegistry.registerRoute {
		webSocket("/api/streaming") {
			throw ApiException(HttpStatusCode.NotImplemented)

//			for (frame in incoming) {
//				val text = (frame as? Frame.Text)?.readText() ?: continue
//				val message = jsonConfig.decodeFromString<StreamingMessage>(text)
//
//				var authenticatedUser: UserEntity? = null
//				val subscriptions = mutableListOf<String>()
//
//				// todo: fix effekt not being suspend
//				suspend fun sendMessage(message: StreamingMessage) {
//					send(Frame.Text(jsonConfig.encodeToString(message)))
//				}
//
//				fun postAuthentication() {
//					if (authenticatedUser == null) return
//
//					val following = RelationshipService.getFollowing(authenticatedUser!!)
//
//					effect<NotificationCreateEvent> { event ->
//						if (event.notification.to.id == authenticatedUser!!.id.toString()) {
//							sendMessage(StreamingStreamEventResponse(
//								stream = "notification",
//								notification = event.notification,
//							))
//							return@effect
//						}
//					}
//
//					effect<NoteCreateEvent> { event ->
//						// local AND is public
//						val showLocal = event.note.user.host == null &&
//							event.note.visibility == Visibility.Public
//
//						// local OR followers post that isn't direct OR note.to contains userid
//						val showHome = showLocal || (following.any { it.id == event.note.user.id } &&
//							(event.note.visibility != Visibility.Direct)) ||
//							(event.note.to?.contains(authenticatedUser!!.id.toString()) ?: false)
//
//						// local OR bubble instance user
//						val showBubble = showLocal || Configuration.timeline.bubble.hosts.contains(event.note.user.host)
//
//						// is public
//						val showPublic = (event.note.visibility == Visibility.Public)
//
//						if (subscriptions.contains("timeline:home") && showHome)
//							sendMessage(StreamingStreamEventResponse(
//								stream = "timeline:home",
//								note = event.note,
//							))
//
//						if (subscriptions.contains("timeline:local") && showLocal)
//							sendMessage(StreamingStreamEventResponse(
//								stream = "timeline:local",
//								note = event.note,
//							))
//
//						if (subscriptions.contains("timeline:bubble") && showBubble)
//							sendMessage(StreamingStreamEventResponse(
//								stream = "timeline:bubble",
//								note = event.note,
//							))
//
//						if (subscriptions.contains("timeline:public") && showPublic)
//							sendMessage(StreamingStreamEventResponse(
//								stream = "timeline:public",
//								note = event.note,
//							))
//					}
//				}
//
//				when (message.type) {
//					StreamingMessageType.Auth -> {
//						val message = jsonConfig.decodeFromString<StreamingAuthRequest>(text)
//						authenticatedUser = AuthService.getByToken(message.token)?.user
//						if (authenticatedUser != null) postAuthentication()
//					}
//
//					StreamingMessageType.Subscribe -> {
//						val message = jsonConfig.decodeFromString<StreamingSubscribeRequest>(text)
//						subscriptions.add(message.stream)
//					}
//
//					StreamingMessageType.Unsubscribe -> {
//						val message = jsonConfig.decodeFromString<StreamingUnsubscribeRequest>(text)
//						subscriptions.remove(message.stream)
//					}
//
//					else -> {}
//				}
//			}
		}
	}
}
