package site.remlit.aster.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.notInList
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.DeliverQueueEntity
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.DeliverQueueTable
import site.remlit.aster.db.table.InboxQueueTable
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.QueueStatus
import site.remlit.aster.model.Service
import site.remlit.aster.registry.InboxHandlerRegistry
import site.remlit.aster.service.ap.ApDeliverService
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Service for managing persistent queues.
 *
 * @since 2025.10.1.0-SNAPSHOT
 * */
object QueueService : Service {
	private val logger = LoggerFactory.getLogger(QueueService::class.java)

	/**
	 * Inbox queue coroutine scope
	 * */
	@JvmStatic
	val inboxScope = CoroutineScope(Dispatchers.Default + CoroutineName("InboxDispatcher"))

	/**
	 * Deliver queue coroutine scope
	 * */
	@JvmStatic
	val deliverScope = CoroutineScope(Dispatchers.Default + CoroutineName("DeliverDispatcher"))

	/**
	 * Current count of active inbox queue workers
	 * */
	@JvmStatic
	var activeInboxWorkers = 0

	/**
	 * Current count of active deliver queue workers
	 * */
	@JvmStatic
	var activeDeliverWorkers = 0

	/**
	 * Currently being processed activities
	 * */
	@JvmStatic
	var lockedIds = mutableSetOf<String>()

	/**
	 * Initialize queue managers. These check frequently for new items in the queue, and then launch a consumer.
	 * */
	@ApiStatus.Internal
	fun initialize() {
		inboxScope.launch {
			while (true) {
				delay(1.seconds)
				summonInboxConsumersIfNeeded()
			}
		}
		deliverScope.launch {
			while (true) {
				delay(1.seconds)
				summonDeliverConsumersIfNeeded()
			}
		}

		logger.info("Initialized inbox and deliver queues")
	}

	// getters

	/**
	 * Gets an inbox job.
	 *
	 * @param where Query to find inbox job
	 *
	 * @return Inbox job
	 * */
	fun getInboxJob(where: Op<Boolean>): InboxQueueEntity? =
		transaction {
			InboxQueueEntity
				.find { where }
				.singleOrNull()
		}

	/**
	 * Gets a deliver job.
	 *
	 * @param where Query to find deliver job
	 *
	 * @return Deliver job
	 * */
	fun getDeliverJob(where: Op<Boolean>): DeliverQueueEntity? =
		transaction {
			DeliverQueueEntity
				.find { where }
				.singleOrNull()
		}

	// queue checkers

	@OptIn(ExperimentalTime::class)
	private fun summonInboxConsumersIfNeeded() {
		transaction {
			InboxQueueEntity
				.find {
					(InboxQueueTable.status eq QueueStatus.PENDING) and (InboxQueueTable.id notInList lockedIds) or
							(InboxQueueTable.status eq QueueStatus.FAILED and (InboxQueueTable.retryAt lessEq TimeService.now()))
				}
				.take(Configuration.queue.inbox.concurrency)
				.toList()
				.forEach {
					if (activeInboxWorkers >= Configuration.queue.inbox.concurrency)
						return@forEach

					inboxScope.launch {
						activeInboxWorkers++
						lockedIds.add(it.id.toString())
						consumeInboxJob(it)
						lockedIds.remove(it.id.toString())
						activeInboxWorkers--
					}
				}
		}
	}

	private fun summonDeliverConsumersIfNeeded() {
		transaction {
			DeliverQueueEntity
				.find {
					(DeliverQueueTable.status eq QueueStatus.PENDING) and (DeliverQueueTable.id notInList lockedIds) or
							(DeliverQueueTable.status eq QueueStatus.FAILED and (DeliverQueueTable.retryAt lessEq TimeService.now()))
				}
				.take(Configuration.queue.deliver.concurrency)
				.toList()
				.forEach {
					if (activeDeliverWorkers >= Configuration.queue.deliver.concurrency)
						return@forEach

					deliverScope.launch {
						activeDeliverWorkers++
						lockedIds.add(it.id.toString())
						consumeDeliverJob(it)
						lockedIds.remove(it.id.toString())
						activeDeliverWorkers--
					}
				}
		}
	}

	// queue consumers

	private fun consumeInboxJob(job: InboxQueueEntity) =
		InboxHandlerRegistry.handle(job)

	private fun consumeDeliverJob(job: DeliverQueueEntity) =
		runBlocking { ApDeliverService.handle(job) }

	// send jobs

	/**
	 * Creates an inbox job to be processed when the next queue worker
	 * is available.
	 *
	 * @param data Byte array of inbox data
	 * @param sender Sender of inbox data
	 * */
	@ApiStatus.Internal
	fun insertInboxJob(
		data: ByteArray,
		sender: UserEntity?
	) {
		transaction {
			InboxQueueEntity.new(IdentifierService.generate()) {
				this.status = QueueStatus.PENDING
				this.content = ExposedBlob(data)
				this.sender = sender
				this.retries = 0
			}
		}
	}

	/**
	 * Creates a deliver job to be processed when the next queue worker
	 * is available.
	 *
	 * @param data Byte array of delivery data
	 * @param sender Sender of delivery data
	 * @param inbox Recipient inbox for delivery
	 * */
	@ApiStatus.Internal
	fun insertDeliverJob(
		data: ByteArray,
		sender: UserEntity?,
		inbox: String
	) {
		transaction {
			DeliverQueueEntity.new(IdentifierService.generate()) {
				this.status = QueueStatus.PENDING
				this.content = ExposedBlob(data)
				this.sender = sender
				this.inbox = inbox
				this.retries = 0
			}
		}
	}

	// complete job

	/**
	 * Marks an inbox job as complete.
	 *
	 * @param job Inbox queue job
	 * */
	@ApiStatus.Internal
	fun completeInboxJob(job: InboxQueueEntity) =
		transaction {
			job.status = QueueStatus.COMPLETED
			job.flush()
		}

	/**
	 * Marks a deliver job as complete.
	 *
	 * @param job Deliver queue job
	 * */
	@ApiStatus.Internal
	fun completeDeliverJob(job: DeliverQueueEntity) =
		transaction {
			job.status = QueueStatus.COMPLETED
			job.flush()
		}

	// error job

	/**
	 * Marks an inbox job as errored, and schedules it to be retried.
	 *
	 * @param job Inbox queue job
	 * @param exception Exception thrown
	 * */
	@ApiStatus.Internal
	@OptIn(ExperimentalTime::class)
	fun errorInboxJob(job: InboxQueueEntity, exception: Exception) =
		transaction {
			logger.error("Inbox job ${job.id} failed: ${exception.message?.replace("\n", "")}")
			job.refresh()
			InboxQueueEntity.findByIdAndUpdate(job.id.toString()) {
				job.status = QueueStatus.FAILED
				it.stacktrace = exception.stackTraceToString()
				job.retryAt = Clock.System.now().plus((job.retries * 15).minutes)
					.toLocalDateTime(TimeZone.currentSystemDefault())
				job.retries += 1
			}
		}

	/**
	 * Marks a deliver job as errored, and schedules it to be retried.
	 *
	 * @param job Deliver queue job
	 * @param exception Exception thrown
	 * */
	@ApiStatus.Internal
	@OptIn(ExperimentalTime::class)
	fun errorDeliverJob(job: DeliverQueueEntity, exception: Exception) =
		transaction {
			logger.error("Deliver job ${job.id} failed: ${exception.message?.replace("\n", "")}")
			job.refresh()
			DeliverQueueEntity.findByIdAndUpdate(job.id.toString()) {
				it.status = QueueStatus.FAILED
				it.stacktrace = exception.stackTraceToString()
				it.retryAt = Clock.System.now().plus((job.retries * 15).minutes)
					.toLocalDateTime(TimeZone.currentSystemDefault())
				it.retries += 1
			}
		}
}
