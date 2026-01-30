package site.remlit.aster.registry

import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.exception.GracefulInboxException
import site.remlit.aster.model.ap.InboxHandler
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.InboxPreprocessor
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
	val inboxPreprocessors = mutableSetOf<InboxPreprocessor>()

	@JvmStatic
	val inboxHandlers = mutableMapOf<String, InboxHandler>()

	/**
	 * Handle preprocessing for an inbox job
	 *
	 * @param job Job to preprocess
	 *
	 * @return Processed job
	 * */
	suspend fun handlePreprocess(job: InboxQueueEntity): InboxQueueEntity? {
		var modifiedJob: InboxQueueEntity? = job

		logger.debug("Preprocessing for inbox job {}", job.id)

		inboxPreprocessors.forEach {
			modifiedJob = it.preprocess(modifiedJob)
			if (modifiedJob == null) return@forEach
			logger.debug("Running preprocessor {} on inbox job {}", it::class.qualifiedName, modifiedJob.id)
		}

		if (modifiedJob == null)
			logger.debug("Inbox job {} cancelled by preprocessing", job.id)

		return modifiedJob
	}

	/**
	 * Handle running an inbox job
	 *
	 * @param job Job to run
	 * */
	@ApiStatus.Internal
	fun handle(job: InboxQueueEntity) {
		val job = runBlocking { handlePreprocess(job) }
			?: return

		val typedObject = jsonConfig.decodeFromString<ApTypedObject>(String(job.content.bytes))

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
					if (handler.key == typedObject.type)
						handler.value.handle(job)
				}
				QueueService.completeInboxJob(job)
			} catch (e: GracefulInboxException) {
				logger.debug("Inbox job {} gracefully failed: {}", job.id, e.message?.replace("\n", ""))
				QueueService.completeInboxJob(job)
			} catch (e: Throwable) {
				QueueService.errorInboxJob(job, e)
			}
		}
	}

	/**
	 * Registers an inbox preprocessor
	 *
	 * @param preprocessor Preprocessor for inbox jobs
	 * */
	@JvmStatic
	fun registerPreprocessor(preprocessor: InboxPreprocessor) {
		inboxPreprocessors.add(preprocessor)
		logger.debug("Added inbox preprocessor ${preprocessor::class.simpleName}")
	}

	/**
	 * Registers an inbox preprocessor
	 * */
	@JvmSynthetic
	inline fun <reified T : InboxPreprocessor> registerPreprocessor() =
		registerPreprocessor(T::class.createInstance())

	/**
	 * Registers an inbox handler
	 *
	 * @param type Activity type to handle
	 * @param handler Handler for activity
	 * */
	@JvmStatic
	fun register(type: String, handler: InboxHandler) {
		inboxHandlers[type] = handler
		logger.debug("Added $type activity handler ${handler::class.simpleName}")
	}

	/**
	 * Registers an inbox handler
	 *
	 * @param type Activity type to handle
	 * */
	@JvmSynthetic
	inline fun <reified T : InboxHandler> register(type: String) =
		register(type, T::class.createInstance())

	/**
	 * Registers default inbox handlers
	 * */
	@ApiStatus.Internal
	internal fun registerDefaults() {
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
