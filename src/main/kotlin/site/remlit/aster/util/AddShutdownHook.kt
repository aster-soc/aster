package site.remlit.aster.util

/**
 * Adds a shutdown hook using the Runtime.getRuntime() method
 *
 * @param block Block to execute
 * */
fun addShutdownHook(block: () -> Unit) {
	Runtime.getRuntime().addShutdownHook(Thread {
		Thread.currentThread().name = "ShutdownHook"
		return@Thread block()
	})
}
