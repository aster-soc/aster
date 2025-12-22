package site.remlit.aster.common.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun LocalDateTime.toLocalInstant(): Instant {
	return this.toInstant(TimeZone.currentSystemDefault())
}

fun Instant.toLocalDateTime(): LocalDateTime {
	return this.toLocalDateTime(TimeZone.currentSystemDefault())
}
