package site.remlit.aster.common.util

inline fun <T> orNull(block: () -> T): T? = try {
	block()
} catch (_: Throwable) {
	null
}
