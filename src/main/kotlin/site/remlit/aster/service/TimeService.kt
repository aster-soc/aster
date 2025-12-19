package site.remlit.aster.service

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import site.remlit.aster.model.Service
import java.time.temporal.ChronoUnit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service for time related utilities.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
@OptIn(ExperimentalTime::class)
object TimeService : Service {
	/**
	 * Gets current LocalDateTime
	 *
	 * @return Current LocalDateTime
	 * */
	@JvmStatic
	fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

	/**
	 * Creates a LocalDateTime a specified amount of seconds in the past.
	 *
	 * @param seconds Number of seconds in the past
	 *
	 * @return Created LocalDateTime
	 * */
	@JvmStatic
	fun secondsAgo(seconds: Long): LocalDateTime {
		return now()
			.toJavaLocalDateTime().minus(seconds, ChronoUnit.SECONDS).toKotlinLocalDateTime()
	}

	/**
	 * Creates a LocalDateTime a specified amount of minutes in the past.
	 *
	 * @param minutes Number of minutes in the past
	 *
	 * @return Created LocalDateTime
	 * */
	@JvmStatic
	fun minutesAgo(minutes: Long): LocalDateTime = secondsAgo(minutes * 60)

	/**
	 * Creates a LocalDateTime a specified amount of hours in the past.
	 *
	 * @param hours Number of hours in the past
	 *
	 * @return Created LocalDateTime
	 * */
	@JvmStatic
	fun hoursAgo(hours: Long): LocalDateTime = minutesAgo(hours * 60)

	/**
	 * Creates a LocalDateTime a specified amount of days in the past.
	 *
	 * @param days Number of days in the past
	 *
	 * @return Created LocalDateTime
	 * */
	@JvmStatic
	fun daysAgo(days: Long): LocalDateTime = hoursAgo(days * 24)

	/**
	 * Creates a LocalDateTime a specified amount of weeks in the past.
	 *
	 * @param weeks Number of weeks in the past
	 *
	 * @return Created LocalDateTime
	 * */
	@JvmStatic
	fun weeksAgo(weeks: Long): LocalDateTime = daysAgo(weeks * 7)
}
