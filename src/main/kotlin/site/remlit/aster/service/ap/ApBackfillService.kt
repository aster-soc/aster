package site.remlit.aster.service.ap

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import site.remlit.aster.common.util.extractString
import site.remlit.aster.common.util.orNull
import site.remlit.aster.db.entity.BackfillQueueEntity
import site.remlit.aster.model.BackfillType
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApOrderedCollection
import site.remlit.aster.model.ap.ApOrderedCollectionPage
import site.remlit.aster.service.QueueService
import site.remlit.aster.service.ResolverService
import site.remlit.aster.util.jsonConfig

/**
 * Service for backfilling information from remote servers
 *
 * @since 2026.1.1.0-SNAPSHOT
 * */
object ApBackfillService {
	private val logger: Logger = LoggerFactory.getLogger(ApBackfillService::class.java)

	/**
	 * Fetches replies from a note
	 *
	 * @param collection AP ID of collection of replies
	 * */
	@JvmStatic
	suspend fun getReplies(collection: String) {
		val json = ResolverService.resolveSigned(collection)
			?: return

		val obj = jsonConfig.decodeFromJsonElement<ApOrderedCollection>(json)

		logger.debug("Object $obj")

		obj.orderedItems.forEach {
			QueueService.insertBackfillJob(
				BackfillType.Fetch,
				it
			)
		}

		if (obj.first != null && obj.orderedItems.isEmpty()) {
			val firstJson = ResolverService.resolveSigned(obj.first)
				?: return

			val firstObj = jsonConfig.decodeFromJsonElement<ApOrderedCollectionPage>(firstJson)

			firstObj.orderedItems.forEach {
				QueueService.insertBackfillJob(
					BackfillType.Fetch,
					it
				)
			}
		}
	}

	/**
	 * Handle a backfill queue job
	 *
	 * @param job Backfill queue job
	 * */
	@JvmStatic
	suspend fun handle(job: BackfillQueueEntity) {
		try {
			when (job.backfillType) {
				BackfillType.Fetch -> ApNoteService.resolve(job.target)
			}

			QueueService.completeBackfillJob(job)
		} catch (e: Throwable) {
			QueueService.errorBackfillJob(job, e)
		}
	}
}
