package site.remlit.aster.event.user

import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.common.model.User
import site.remlit.aster.model.event.Event

/**
 * Event related to a User
 *
 * @since 2025.9.1.0-SNAPSHOT
 * */
@ApiStatus.OverrideOnly
open class UserEvent(val user: User) : Event
