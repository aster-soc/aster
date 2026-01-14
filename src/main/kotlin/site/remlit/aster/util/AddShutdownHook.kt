package site.remlit.aster.util

private var shutdownHookCount = 1

/**
 * Adds a shutdown hook using the Runtime.getRuntime() method
 *
 * @param block Block to execute
 * */
fun addShutdownHook(block: () -> Unit) =
	Runtime.getRuntime().addShutdownHook(Thread {
		Thread.currentThread().name = "ShutdownHook-$shutdownHookCount"
		shutdownHookCount++
		return@Thread block()
	})
