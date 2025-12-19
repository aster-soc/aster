package site.remlit.aster.event.user

import site.remlit.aster.common.model.User

/**
 * Event fired when a user's password is reset
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
class UserPasswordResetEvent(user: User) : UserEvent(user)
