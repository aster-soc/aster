package site.remlit.aster.event.auth

import site.remlit.aster.db.entity.AuthEntity

/**
 * Event for when an auth token is used
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
class AuthTokenUseEvent(auth: AuthEntity) : AuthEvent(auth)
