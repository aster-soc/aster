package site.remlit.aster.common.util

inline fun <T> ifFails(block: () -> T, backup: () -> T): T =
	try {
		block()
	} catch (_: Exception) {
		backup()
	}
