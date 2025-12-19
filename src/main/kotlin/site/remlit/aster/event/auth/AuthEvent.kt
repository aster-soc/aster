package site.remlit.aster.event.auth

import site.remlit.aster.db.entity.AuthEntity
import site.remlit.aster.model.event.Event

/**
 * Event related to authentication
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
open class AuthEvent(val auth: AuthEntity) : Event
