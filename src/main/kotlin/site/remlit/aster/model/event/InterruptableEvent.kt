package site.remlit.aster.model.event

import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.registry.EventRegistry

/**
 * Interface for events that's interruptable.
 *
 * Upon interrupting, the remaining listeners will not be
 * executed.
 *
 * @since 2025.12.1.0-SNAPSHOT
 * */
@ApiStatus.OverrideOnly
interface InterruptableEvent : Event {
	/**
	 * Interrupt event
	 * */
	fun interrupt() = EventRegistry.interruptEvent(this)
}
