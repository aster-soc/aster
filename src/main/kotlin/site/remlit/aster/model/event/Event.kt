package site.remlit.aster.model.event

import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.registry.EventRegistry

/**
 * Interface for listenable events
 * */
@ApiStatus.OverrideOnly
interface Event {
	/**
	 * Execute event
	 * */
	fun call() = EventRegistry.executeEvent(this)
}
