package site.remlit.aster.event.relationship

import site.remlit.aster.common.model.Relationship

/**
 * Event fired when a relationship is deleted
 *
 * @since 2026.1.3.0-SNAPSHOT
 * */
class RelationshipDeleteEvent(relationship: Relationship) : RelationshipEvent(relationship)

