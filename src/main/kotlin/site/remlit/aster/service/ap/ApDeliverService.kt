package site.remlit.aster.service.ap

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.http.*
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.type.PolicyType
import site.remlit.aster.db.entity.DeliverQueueEntity
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.exception.ResolverException
import site.remlit.aster.model.ap.DeliverPreprocessor
import site.remlit.aster.registry.InboxHandlerRegistry
import site.remlit.aster.registry.InboxHandlerRegistry.inboxPreprocessors
import site.remlit.aster.service.KeypairService
import site.remlit.aster.service.PolicyService
import site.remlit.aster.service.QueueService
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.ResolverService
import site.remlit.aster.service.ResolverService.okRange
import site.remlit.aster.service.UserService
import site.remlit.aster.util.jsonConfig
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.log
import kotlin.reflect.full.createInstance

/**
 * Service for handling activity delivery
 *
 * @since 2025.11.2.0-SNAPSHOT
 * */
object ApDeliverService {
	private val logger = LoggerFactory.getLogger(ApDeliverService::class.java)

	@JvmStatic
	val deliverPreprocessors = mutableSetOf<DeliverPreprocessor>()

	/**
	 * Registers a deliver preprocessor
	 *
	 * @param preprocessor Preprocessor for deliver job
	 * */
	@JvmStatic
	fun registerPreprocessor(preprocessor: DeliverPreprocessor) {
		deliverPreprocessors.add(preprocessor)
		logger.debug("Added deliver preprocessor ${preprocessor::class.qualifiedName}")
	}

	/**
	 * Registers a deliver preprocessor
	 * */
	@JvmSynthetic
	inline fun <reified T : DeliverPreprocessor> registerPreprocessor() {
		registerPreprocessor(T::class.createInstance())
	}

	/**
	 * Handles preprocessing for a deliver job
	 *
	 * @param job Job to preprocess
	 *
	 * @return Processed job
	 * */
	@JvmStatic
	suspend fun handlePreprocessing(job: DeliverQueueEntity): DeliverQueueEntity? {
		var modifiedJob: DeliverQueueEntity? = job

		logger.debug("Preprocessing for deliver job {}", job.id)

		deliverPreprocessors.forEach {
			modifiedJob = it.preprocess(modifiedJob)
			if (modifiedJob == null) return@forEach
			logger.debug("Running preprocessor {} on deliver job {}", it::class.qualifiedName, modifiedJob.id)
		}

		if (modifiedJob == null)
			logger.debug("Deliver job {} cancelled by preprocessing", job.id)

		return modifiedJob
	}

	// TODO: Non-synthetic delivery methods

	/**
	 * Deliver an activity to an inbox
	 *
	 * @param activity Activity to send
	 * @param sender Activity sender
	 * @param inbox Inbox to deliver to
	 * */
	@JvmSynthetic
	inline fun <reified T> deliver(
		activity: T,
		sender: UserEntity?,
		inbox: String
	) = QueueService.insertDeliverJob(
			jsonConfig.encodeToString<T>(activity).encodeToByteArray(),
			sender,
			inbox
		)

	/**
	 * Deliver an activity to the inboxes of followers of
	 * the sender.
	 *
	 * @param activity Activity to send
	 * @param sender Activity sender
	 * @param and Other inboxes to send to
	 * */
	@JvmSynthetic
	inline fun <reified T> deliverToFollowers(
		activity: T,
		sender: UserEntity,
		and: List<String> = listOf()
	) {
		val inboxes = RelationshipService.getFollowers(sender).map { it.inbox }
			.toMutableList().apply { this.addAll(and) }.distinct()

		for (inbox in inboxes) {
			deliver<T>(activity, sender, inbox)
		}
	}

	/**
	 * Handle a deliver job
	 *
	 * @param job Job to handle
	 * */
	@JvmStatic
	suspend fun handle(job: DeliverQueueEntity) {
		val job = handlePreprocessing(job) ?: return

		try {
			val url = Url(job.inbox)

			val date = LocalDateTime.now(ZoneId.of("GMT"))
				.toHttpDateString()

			val blockPolicies = PolicyService.getAllByType(PolicyType.Block)
			val blockedHosts = PolicyService.reducePoliciesToHost(blockPolicies)

			if (blockedHosts.contains(url.host))
				return

			val actor = transaction { job.sender } ?: UserService.getInstanceActor()

			val actorPrivate = UserService.getPrivateById(actor.id.toString())!!

			val digest = "SHA-256=${ApSignatureService.createDigest(job.content.bytes)}"

			val client = ResolverService.createClient()
			val response = client.post(url) {
				headers.append("Host", url.host)
				headers.append("Date", date)
				headers.append("Digest", digest)
				headers.append("Content-Type", "application/activity+json")

				setBody(job.content.bytes)

				val sig = ApSignatureService.createSignature(
					url.encodedPath,
					HttpMethod.Post,
					KeypairService.pemToPrivateKey(actorPrivate.privateKey),
					actor.apId + "#main-key",
					mapOf(
						"Host" to listOf(url.host),
						"Date" to listOf(date),
						"Digest" to listOf(digest),
						"Content-Type" to listOf("application/activity+json")
					)
				)

				headers.append("Signature", sig.first)
			}
			client.close()

			if (response.status.value !in okRange)
				throw ResolverException(response.status, response.status.description)
			else {
				logger.info("${response.status} ${response.request.method} - ${response.request.url}")
				QueueService.completeDeliverJob(job)
			}
		} catch (e: Exception) {
			QueueService.errorDeliverJob(job, e)
		}
	}
}
