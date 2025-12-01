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
import site.remlit.aster.service.ap.inbox.ApBiteHandler
import site.remlit.aster.service.ap.inbox.ApCreateHandler
import site.remlit.aster.service.ap.inbox.ApFollowHandler
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
			} catch (e: Exception) {
				e.printStackTrace()
				logger.error("Job ${job.id} failed: ${e.message?.replace("\n", "")}")
				QueueService.errorInboxJob(job)
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
		register<ApBiteHandler>("Bite")
		register<ApCreateHandler>("Create")
		register<ApFollowHandler>("Follow")
	}
}
