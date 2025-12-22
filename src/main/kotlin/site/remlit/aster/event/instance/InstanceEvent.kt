package site.remlit.aster.event.instance

import site.remlit.aster.common.model.Instance
import site.remlit.effekt.Event

/**
 * Event related to an instance
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
open class InstanceEvent(val instance: Instance) : Event
