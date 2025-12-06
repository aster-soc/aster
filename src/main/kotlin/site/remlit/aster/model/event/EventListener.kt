package site.remlit.aster.model.event

import kotlin.reflect.KClass

data class EventListener(
	val priority: ListenerPriority,
	val event: KClass<*>,
	val listener: (Event) -> Unit,
)
