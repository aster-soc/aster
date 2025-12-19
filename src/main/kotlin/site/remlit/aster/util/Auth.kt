package site.remlit.aster.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.event.auth.AuthTokenUseEvent
import site.remlit.aster.model.ApiException
import site.remlit.aster.service.AuthService
import site.remlit.aster.service.RoleService
import site.remlit.aster.service.ap.ApValidationService
import site.remlit.aster.util.auth.AuthRouteSelector

val authenticatedUserKey = AttributeKey<UserEntity>("authenticatedUser")
val authenticatedUserRoleKey = AttributeKey<RoleType>("authenticatedUserRole")
val authorizedFetchUserKey = AttributeKey<UserEntity>("authorizedFetchUser")

/**
 * Internal shared logic for authentication plugins.
 *
 * @param optional If authentication is optional
 * @param request Call request
 * @param attributes Request attributes
 * @param authorizedFetch If this should use ApValidationService to authenticate instead
 * @param requiredRole Role to require as part of regular authentication
 *
 * @since 2025.11.3.0-SNAPSHOT
 * */
@ApiStatus.Internal
@Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
private fun internalAuth(
	optional: Boolean,
	request: ApplicationRequest,
	attributes: Attributes,
	authorizedFetch: Boolean = false,
	requiredRole: RoleType? = null,
) {
	if (authorizedFetch) {
		val viewer = try {
			runBlocking { ApValidationService.validate(request as RoutingRequest) }
		} catch (e: Exception) {
			if (!optional) throw e
			else null
		}

		if (viewer != null && viewer.activated && !viewer.suspended) {
			attributes.put(
				authorizedFetchUserKey,
				viewer
			)
		}
	} else {
		val authHeader = request.headers["Authorization"]
		val authCookie = request.cookies["AsAuthorization"]
		val token = authHeader?.replace("Bearer ", "") ?: authCookie

		val authEntity = if (optional && token != null) {
			AuthService.getByToken(
				token
			)
		} else if (!optional) {
			AuthService.getByToken(
				token ?: throw ApiException(
                    HttpStatusCode.Unauthorized,
                    "Authentication failed"
                )
			) ?: throw ApiException(
                HttpStatusCode.Unauthorized,
                "Authentication failed"
            )
		} else {
			null
		}

		val user = transaction { authEntity?.user }

		if (user != null && user.activated && !user.suspended) {
			attributes.put(
				authenticatedUserKey,
				user
			)

			val highestRole = RoleService.getUserHighestRole(user.id.toString()) ?: RoleType.Normal
			attributes.put(
				authenticatedUserRoleKey,
				highestRole
			)

			if (requiredRole != null) {
				when (requiredRole) {
					RoleType.Mod -> if (highestRole != RoleType.Admin && highestRole != RoleType.Mod)
						throw ApiException(
                            HttpStatusCode.Unauthorized,
                            "Authentication failed"
                        )

					RoleType.Admin -> if (highestRole != RoleType.Admin)
						throw ApiException(
                            HttpStatusCode.Unauthorized,
                            "Authentication failed"
                        )

					else -> {}
				}
			}
		}

        if (authEntity != null) AuthTokenUseEvent(authEntity).call()
	}
}

/**
 * Used for authentication of routes.
 *
 * Added to the request attributes will be `authenticatedUserKey`, which
 * has a UserEntity of the requesting user, if applicable.
 * Alongside that will be a `authenticatedUserRoleKey` for the user's highest
 * role.
 *
 * @param required If authentication is required to access routes
 * @param role Role type required to access routes
 * @param route Route block
 *
 * @since 2025.11.3.0-SNAPSHOT
 *
 * @return Route block wrapped in authentication
 * */
fun Route.authentication(
	required: Boolean = false,
	role: RoleType? = null,
	route: Route.() -> Unit
): Route {
	val authRoute = createChild(AuthRouteSelector())

	if (!required) {
		authRoute.install(AuthOptional)
	} else {
		when (role) {
			RoleType.Admin -> authRoute.install(AuthRequiredAdmin)
			RoleType.Mod -> authRoute.install(AuthRequiredMod)
			else -> authRoute.install(AuthRequired)
		}
	}
	authRoute.route()

	return authRoute
}

private val AuthOptional = createRouteScopedPlugin("authOptional") {
	onCall { call ->
		internalAuth(
			true,
			call.request,
			call.attributes,
		)
	}
}

private val AuthRequired = createRouteScopedPlugin("authRequired") {
	onCall { call ->
		internalAuth(
			false,
			call.request,
			call.attributes,
		)
	}
}

private val AuthRequiredAdmin = createRouteScopedPlugin("authRequiredAdmin") {
	onCall { call ->
		internalAuth(
			false,
			call.request,
			call.attributes,
			requiredRole = RoleType.Admin
		)
	}
}

private val AuthRequiredMod = createRouteScopedPlugin("authRequiredMod") {
	onCall { call ->
		internalAuth(
			false,
			call.request,
			call.attributes,
			requiredRole = RoleType.Mod
		)
	}
}

/**
 * Used for authentication of AP routes.
 * Simplifies calls to Ktor's Authentication plugin.
 *
 * Added to the request attributes will be `authorizedFetchUserKey`, which
 * has a UserEntity of the requesting user, if applicable.
 *
 * @param required If authentication is required to access routes
 * @param route Route block
 *
 * @since 2025.11.3.0-SNAPSHOT
 *
 * @return Route block wrapped in authentication
 * */
fun Route.authorizedFetch(
	required: Boolean = false,
	route: Route.() -> Unit
): Route {
	val authRoute = createChild(AuthRouteSelector())

	if (!required) authRoute.install(AuthOptionalAuthorizedFetch)
	else authRoute.install(AuthRequiredAuthorizedFetch)

	authRoute.route()

	return authRoute
}


private val AuthRequiredAuthorizedFetch = createRouteScopedPlugin("authRequiredAuthorizedFetch") {
	onCall { call ->
		internalAuth(
			true,
			call.request,
			call.attributes,
			true
		)
	}
}

private val AuthOptionalAuthorizedFetch = createRouteScopedPlugin("authOptionalAuthorizedFetch") {
	onCall { call ->
		internalAuth(
			false,
			call.request,
			call.attributes,
			true
		)
	}
}
