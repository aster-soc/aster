package site.remlit.aster.event.policy

import site.remlit.aster.common.model.Policy
import site.remlit.effekt.Event

/**
 * Event related to a Policy
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
open class PolicyEvent(val policy: Policy) : Event
