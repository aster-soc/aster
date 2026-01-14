package site.remlit.aster.util

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import site.remlit.aster.util.detachedScope

val detachedScope = CoroutineScope(Dispatchers.Default + CoroutineName("Detached"))

/**
 * Launch a detached task
 *
 * @param block Code to run
 * */
inline fun detached(crossinline block: () -> Unit) =
	detachedScope.launch { block() }
