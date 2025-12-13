package site.remlit.aster.util

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Launch a detached task
 *
 * @param block Code to run
 * */
inline fun detached(crossinline block: () -> Unit) =
	runBlocking { launch { block() } }
