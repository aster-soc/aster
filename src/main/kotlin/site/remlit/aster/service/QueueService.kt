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
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.notInList
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.BackfillQueueEntity
import site.remlit.aster.db.entity.DeliverQueueEntity
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.BackfillQueueTable
import site.remlit.aster.db.table.DeliverQueueTable
import site.remlit.aster.db.table.InboxQueueTable
import site.remlit.aster.model.BackfillType
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.QueueStatus
import site.remlit.aster.model.Service
import site.remlit.aster.registry.InboxHandlerRegistry
import site.remlit.aster.service.ap.ApBackfillService
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
@Suppress("TooManyFunctions")
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
	 * Backfill queue coroutine scope
	 * */
	@JvmStatic
	val backfillScope = CoroutineScope(Dispatchers.Default + CoroutineName("BackfillDispatcher"))

	/**
	 * Deliver queue coroutine scope
	 * */
	@JvmStatic
	val cleanerScope = CoroutineScope(Dispatchers.Default + CoroutineName("QueueCleanerDispatcher"))

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
	 * Current count of active backfill queue workers
	 * */
	@JvmStatic
	var activeBackfillWorkers = 0

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
				summonInboxConsumersIfNeeded()
				delay(1.seconds)
			}
		}
		deliverScope.launch {
			while (true) {
				summonDeliverConsumersIfNeeded()
				delay(1.seconds)
			}
		}
		backfillScope.launch {
			while (true) {
				summonBackfillConsumersIfNeeded()
				delay(1.seconds)
			}
		}
		cleanerScope.launch {
			while (true) {
				clean()
				delay(30.minutes)
			}
		}

		logger.info("Initialized inbox and deliver queues")
	}

	/**
	 * Cleans old completed jobs and jobs that cannot be retried.
	 * */
	@Suppress("MagicNumber")
	fun clean() {
		var inboxCount = 0
		var deliverCount = 0
		var backfillCount = 0

		transaction {
			InboxQueueEntity.find((InboxQueueTable.status eq QueueStatus.COMPLETED or
				(InboxQueueTable.retries greaterEq Configuration.queue.inbox.maxRetries)) and
				(InboxQueueTable.createdAt greater TimeService.daysAgo(3))).forEach { it.delete(); inboxCount++ }

			DeliverQueueEntity.find((DeliverQueueTable.status eq QueueStatus.COMPLETED or
				(DeliverQueueTable.retries greaterEq Configuration.queue.deliver.maxRetries)) and
				(DeliverQueueTable.createdAt greater TimeService.daysAgo(3))).forEach { it.delete(); deliverCount++ }

			BackfillQueueEntity.find((BackfillQueueTable.status eq QueueStatus.COMPLETED or
				(BackfillQueueTable.retries greaterEq Configuration.queue.backfill.maxRetries)) and
				(BackfillQueueTable.createdAt greater TimeService.daysAgo(3))).forEach { it.delete(); backfillCount++ }
		}

		logger.debug("Queue cleaner ran, $inboxCount inbox jobs, $deliverCount deliver jobs," +
			" and $backfillCount backfill jobs deleted")
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

	/**
	 * Gets a backfill job.
	 *
	 * @param where Query to find backfill job
	 *
	 * @return Backfill job
	 * */
	fun getBackfillJob(where: Op<Boolean>): BackfillQueueEntity? =
		transaction {
			BackfillQueueEntity
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
							(InboxQueueTable.status eq QueueStatus.FAILED and (InboxQueueTable.retryAt lessEq TimeService.now())
									and (InboxQueueTable.retries lessEq Configuration.queue.inbox.maxRetries))
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
							(DeliverQueueTable.status eq QueueStatus.FAILED and (DeliverQueueTable.retryAt lessEq TimeService.now())
									and (DeliverQueueTable.retries lessEq Configuration.queue.deliver.maxRetries))
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

	@OptIn(ExperimentalTime::class)
	private fun summonBackfillConsumersIfNeeded() {
		transaction {
			BackfillQueueEntity
				.find {
					(BackfillQueueTable.status eq QueueStatus.PENDING) and (BackfillQueueTable.id notInList lockedIds) or
						(BackfillQueueTable.status eq QueueStatus.FAILED and (BackfillQueueTable.retryAt lessEq TimeService.now())
							and (BackfillQueueTable.retries lessEq Configuration.queue.backfill.maxRetries))
				}
				.take(Configuration.queue.backfill.concurrency)
				.toList()
				.forEach {
					if (activeBackfillWorkers >= Configuration.queue.backfill.concurrency)
						return@forEach

					inboxScope.launch {
						activeBackfillWorkers++
						lockedIds.add(it.id.toString())
						consumeBackfillJob(it)
						lockedIds.remove(it.id.toString())
						activeBackfillWorkers--
					}
				}
		}
	}

	// queue consumers

	private fun consumeInboxJob(job: InboxQueueEntity) =
		InboxHandlerRegistry.handle(job)

	private fun consumeDeliverJob(job: DeliverQueueEntity) =
		runBlocking { ApDeliverService.handle(job) }

	private fun consumeBackfillJob(job: BackfillQueueEntity) =
		runBlocking { ApBackfillService.handle(job) }

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

	/**
	 * Creates a backfill job to be processed when the next queue worker
	 * is available.
	 *
	 * @param type Type of backfill job
	 * @param target AP ID target of backfill job
	 * */
	@ApiStatus.Internal
	fun insertBackfillJob(
		type: BackfillType,
		target: String,
	) {
		transaction {
			BackfillQueueEntity.new(IdentifierService.generate()) {
				this.status = QueueStatus.PENDING
				this.backfillType = type
				this.target = target
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

	/**
	 * Marks a backfill job as complete.
	 *
	 * @param job Backfill queue job
	 * */
	@ApiStatus.Internal
	fun completeBackfillJob(job: BackfillQueueEntity) =
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
	fun errorInboxJob(job: InboxQueueEntity, exception: Throwable) =
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

	/**
	 * Marks a backfill job as errored, and schedules it to be retried.
	 *
	 * @param job Backfill queue job
	 * @param exception Exception thrown
	 * */
	@ApiStatus.Internal
	@OptIn(ExperimentalTime::class)
	fun errorBackfillJob(job: BackfillQueueEntity, exception: Throwable) =
		transaction {
			logger.error("Backfill job ${job.id} failed: ${exception.message?.replace("\n", "")}")
			job.refresh()
			BackfillQueueEntity.findByIdAndUpdate(job.id.toString()) {
				job.status = QueueStatus.FAILED
				it.stacktrace = exception.stackTraceToString()
				job.retryAt = Clock.System.now().plus((job.retries * 15).minutes)
					.toLocalDateTime(TimeZone.currentSystemDefault())
				job.retries += 1
			}
		}
}
