package site.remlit.aster.event.auth

import site.remlit.aster.db.entity.AuthEntity

/**
 * Event for when an auth token is created
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
class AuthTokenCreateEvent(auth: AuthEntity) : AuthEvent(auth)
