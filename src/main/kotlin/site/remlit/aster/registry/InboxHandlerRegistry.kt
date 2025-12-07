package site.remlit.aster.registry

import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.ap.ApInboxHandler
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.service.QueueService
import site.remlit.aster.service.ap.inbox.ApAcceptHandler
import site.remlit.aster.service.ap.inbox.ApAddHandler
import site.remlit.aster.service.ap.inbox.ApAnnounceHandler
import site.remlit.aster.service.ap.inbox.ApBiteHandler
import site.remlit.aster.service.ap.inbox.ApBlockHandler
import site.remlit.aster.service.ap.inbox.ApCreateHandler
import site.remlit.aster.service.ap.inbox.ApDeleteHandler
import site.remlit.aster.service.ap.inbox.ApEmojiReactHandler
import site.remlit.aster.service.ap.inbox.ApFollowHandler
import site.remlit.aster.service.ap.inbox.ApLikeHandler
import site.remlit.aster.service.ap.inbox.ApRejectHandler
import site.remlit.aster.service.ap.inbox.ApRemoveHandler
import site.remlit.aster.service.ap.inbox.ApUndoHandler
import site.remlit.aster.service.ap.inbox.ApUpdateHandler
import site.remlit.aster.util.jsonConfig
import kotlin.reflect.full.createInstance

object InboxHandlerRegistry {
	private val logger = LoggerFactory.getLogger(InboxHandlerRegistry::class.java)

	@JvmStatic
	val inboxHandlers = mutableListOf<Pair<String, ApInboxHandler>>()

	/**
	 * Handle running an inbox job.
	 *
	 * @param job Job to run
	 * */
	@ApiStatus.Internal
	fun handle(job: InboxQueueEntity) {
		val typedObject = jsonConfig.decodeFromString<ApTypedObject>(String(job.content.bytes))

		if (Configuration.debug)
			logger.debug(
				"[{}] Consuming object of type {} from {} on attempt {}",
				job.id,
				typedObject.type,
				transaction { job.sender?.apId ?: "unknown" },
				job.retries + 1
			)

		runBlocking {
			try {
				for (handler in inboxHandlers) {
					if (handler.first == typedObject.type)
						handler.second.handle(job)
				}
				QueueService.completeInboxJob(job)
			} catch (e: Throwable) {
				QueueService.errorInboxJob(job, e)
			}
		}
	}

	/**
	 * Registers an inbox handler
	 *
	 * @param type Activity type to handle
	 * @param handler Handler for activity
	 * */
	@JvmStatic
	fun register(type: String, handler: ApInboxHandler) {
		inboxHandlers.add(Pair(type, handler))
		if (Configuration.debug) logger.debug("Added $type activity handler ${handler::class.simpleName}")
	}

	/**
	 * Registers an inbox handler
	 *
	 * @param type Activity type to handle
	 * */
	@JvmSynthetic
	inline fun <reified T : ApInboxHandler> register(type: String) =
		register(type, T::class.createInstance())

	/**
	 * Registers default inbox handlers
	 * */
	@ApiStatus.Internal
	fun registerDefaults() {
		register<ApAcceptHandler>("Accept")
		register<ApAddHandler>("Add")
		register<ApAnnounceHandler>("Announce")
		register<ApBiteHandler>("Bite")
		register<ApBlockHandler>("Block")
		register<ApCreateHandler>("Create")
		register<ApDeleteHandler>("Delete")
		register<ApEmojiReactHandler>("EmojiReact")
		register<ApFollowHandler>("Follow")
		register<ApLikeHandler>("Like")
		register<ApRejectHandler>("Reject")
		register<ApRemoveHandler>("Remove")
		register<ApUndoHandler>("Undo")
		register<ApUpdateHandler>("Update")
	}
}
