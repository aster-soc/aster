package site.remlit.aster.event.user

import site.remlit.aster.common.model.User

/**
 * Event fired for when a user enables time-based one time passwords
 *
 * @since 2026.1.4.0-SNAPSHOT
 * */
class UserTotpRegisterEvent(user: User) : UserEvent(user)
