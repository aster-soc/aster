package site.remlit.aster.service

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import site.remlit.aster.model.Service
import java.net.IDN
import kotlin.time.ExperimentalTime

/**
 * Service for formatting various things.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object FormatService : Service {
	/**
	 * Convert string to ASCII string
	 *
	 * @param string Any string
	 *
	 * @return ASCII string
	 * */
	@JvmStatic
	fun toASCII(string: String): String = IDN.toASCII(string)

	/**
	 * Convert LocalDateTime to relative time
	 *
	 * @param value Any LocalDateTime
	 *
	 * @return Relative time string
	 * */
	@JvmStatic
	@OptIn(ExperimentalTime::class)
	fun relativeTime(value: LocalDateTime): String {
		val now = TimeService.now()
		val duration = now.toInstant(TimeZone.currentSystemDefault()) -
				value.toInstant(TimeZone.currentSystemDefault())

		return when {
			duration.inWholeSeconds < 1 -> "now"
			duration.inWholeMinutes < 1 -> "${duration.inWholeSeconds} seconds ago"
			duration.inWholeMinutes < 1 -> "${duration.inWholeSeconds} seconds ago"
			duration.inWholeHours < 1 -> "${duration.inWholeMinutes} minutes ago"
			duration.inWholeDays < 1 -> "${duration.inWholeHours} hours ago"
			else -> "${value.day} ${value.month.name}"
		}
	}
}
