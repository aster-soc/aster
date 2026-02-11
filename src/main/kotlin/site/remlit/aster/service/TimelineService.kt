package site.remlit.aster.service

import kotlinx.datetime.LocalDateTime
import site.remlit.aster.common.util.toLocalDateTime
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Service for timeline related utilities.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object TimelineService : Service {
	/**
	 * Ensures the timeline take is within the acceptable range
	 *
	 * @param take Number provided by a user
	 *
	 * @return Number acceptable for the server to use
	 * */
	@JvmStatic
	fun normalizeTake(take: Int?): Int {
		if (take != null) {
			if (take > Configuration.timeline.maxObjects) {
				return Configuration.timeline.maxObjects
			} else if (take < 1) {
				return 1
			}
			return take
		} else {
			return Configuration.timeline.defaultObjects
		}
	}

	/**
	 * Normalizes since date and time provided by a user
	 *
	 * @param since Time date string provided by a user
	 *
	 * @return Time and date acceptable for the server to use
	 * */
	@JvmStatic
	fun normalizeSince(since: String?): LocalDateTime {
		return (if (since != null) Instant.parse(since) else Clock.System.now())
			.toLocalDateTime()
	}
}
