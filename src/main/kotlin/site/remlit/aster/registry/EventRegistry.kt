package site.remlit.aster.registry

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import org.slf4j.LoggerFactory
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Event
import site.remlit.aster.model.event.EventListener
import site.remlit.aster.model.event.ListenerPriority
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

object EventRegistry {
	private val logger = LoggerFactory.getLogger(EventRegistry::class.java)

	/**
	 * Mutable list of event classes and functions to run when they're called
	 *
	 * @since 2025.9.1.0-SNAPSHOT
	 * */
	@JvmStatic
	val listeners: MutableList<EventListener> = emptyList<EventListener>().toMutableList()

	/**
	 * Adds a function to be called when an event fires
	 *
	 * @param listener Lambda to run when the event happens
	 *
	 * @since 2025.11.3.0-SNAPSHOT
	 * */
	@JvmSynthetic
	@Suppress("UNCHECKED_CAST")
	inline fun <reified T> addListener(noinline listener: (T) -> Unit) =
		addListener(T::class, listener as (Event) -> Unit)

	/**
	 * Adds a function to be called when an event fires
	 *
	 * @param priority Priority of event listener
	 * @param listener Lambda to run when the event happens
	 *
	 * @since 2025.11.3.0-SNAPSHOT
	 * */
	@JvmSynthetic
	@Suppress("UNCHECKED_CAST")
	inline fun <reified T> addListener(priority: ListenerPriority, noinline listener: (T) -> Unit) =
		addListener(T::class, listener as (Event) -> Unit, priority)

	/**
	 * Adds a function to be called when an event fires
	 *
	 * @param event Class of the event to listen to (e.g. `NoteCreateEvent::class`)
	 * @param listener Lambda to run when the event happens
	 * @param priority Priority of event listener
	 *
	 * @since 2025.9.1.0-SNAPSHOT
	 * */
	@JvmStatic
	fun addListener(
		event: KClass<*>,
		listener: (Event) -> Unit
	) = addListener(event, listener, ListenerPriority.Normal)

	/**
	 * Adds a function to be called when an event fires
	 *
	 * @param event Class of the event to listen to (e.g. `NoteCreateEvent::class`)
	 * @param listener Lambda to run when the event happens
	 * @param priority Priority of event listener
	 *
	 * @since 2025.9.1.0-SNAPSHOT
	 * */
	@JvmStatic
	fun addListener(
		event: KClass<*>,
		listener: (Event) -> Unit,
		priority: ListenerPriority
	) {
		if (!event.isSubclassOf(Event::class))
			throw IllegalArgumentException("Event $event is not a derivative of the Event interface")
		if (Configuration.debug) logger.debug(
			"Added ${event.simpleName} listener ${listener::class.simpleName}" +
					"with priority ${priority.name}"
		)
		listeners.add(EventListener(priority, event, listener))
	}

	private val eventScope = CoroutineScope(Dispatchers.Default + CoroutineName("EventCoroutine"))

	/**
	 * Function ran from an Event when it's called through the call() method.
	 *
	 * This function will launch a coroutine that will run listeners for the specified event in order they were registered.
	 *
	 * @param event Instance of an event to be executed
	 *
	 * @since 2025.9.1.0-SNAPSHOT
	 * */
	@ApiStatus.Internal
	fun executeEvent(event: Event) =
		runBlocking {
			if (Configuration.debug) logger.debug("Executing event ${event::class.simpleName}")
			val prioritizedList = listeners.toList().sortedBy { it.priority }
			eventScope.launch {
				for (listener in prioritizedList) {
					if (!listener.event.isInstance(event)) continue
					listener.listener.invoke(event)
				}
			}
		}

	/**
	 * Clears the registered listeners to prevent any events blocking shutdown or not completing correctly.
	 * */
	@ApiStatus.Internal
	fun clearListeners() = listeners.clear()
}
