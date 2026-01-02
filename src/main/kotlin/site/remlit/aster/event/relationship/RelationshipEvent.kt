package site.remlit.aster.event.relationship

import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.common.model.Relationship
import site.remlit.effekt.Event

/**
 * Event related to a relationship
 *
 * @since 2026.1.3.0-SNAPSHOT
 * */
@ApiStatus.OverrideOnly
open class RelationshipEvent(val relationship: Relationship) : Event
