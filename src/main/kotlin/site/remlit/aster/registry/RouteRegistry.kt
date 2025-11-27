package site.remlit.aster.registry

import io.ktor.server.routing.*
import org.jetbrains.annotations.ApiStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import site.remlit.aster.model.Configuration
import site.remlit.aster.route.FrontendRoutes
import site.remlit.aster.route.UploadRoutes
import site.remlit.aster.route.admin.AdminDebugRoutes
import site.remlit.aster.route.admin.AdminIndexRoutes
import site.remlit.aster.route.admin.AdminInstanceRoutes
import site.remlit.aster.route.admin.AdminInviteRoutes
import site.remlit.aster.route.admin.AdminPluginRoutes
import site.remlit.aster.route.admin.AdminQueueRoutes
import site.remlit.aster.route.admin.AdminUserRoutes
import site.remlit.aster.route.ap.ApNoteRoutes
import site.remlit.aster.route.ap.ApUserRoutes
import site.remlit.aster.route.ap.HostMetaRoutes
import site.remlit.aster.route.ap.InboxRoutes
import site.remlit.aster.route.ap.NodeInfoRoutes
import site.remlit.aster.route.ap.OutboxRoutes
import site.remlit.aster.route.ap.WebfingerRoutes
import site.remlit.aster.route.api.DriveRoutes
import site.remlit.aster.route.api.FollowRequestRoutes
import site.remlit.aster.route.api.LoginRoutes
import site.remlit.aster.route.api.MetaRoutes
import site.remlit.aster.route.api.NoteRoutes
import site.remlit.aster.route.api.NotificationRoutes
import site.remlit.aster.route.api.PasswordResetRoutes
import site.remlit.aster.route.api.RegisterRoutes
import site.remlit.aster.route.api.SearchRoutes
import site.remlit.aster.route.api.TimelineRoutes
import site.remlit.aster.route.api.UserRoutes
import site.remlit.aster.route.api.mod.InviteRoutes
import site.remlit.aster.route.api.mod.PolicyRoutes

/**
 * Registry for keeping track of built-in and plugin routes.
 *
 * @since 2025.10.1.0-SNAPSHOT
 */
object RouteRegistry {
	private val logger: Logger = LoggerFactory.getLogger(RouteRegistry::class.java)

	/**
	 * List of registered extension functions for routes.
	 */
	@JvmStatic
	val routes: MutableList<Routing.() -> Unit> = emptyList<Routing.() -> Unit>().toMutableList()

	/**
	 * Register a route to be loaded by the server
	 *
	 * @param route Lambda containing route
	 */
	@JvmStatic
	fun registerRoute(
		route: Routing.() -> Unit
	) {
		if (Configuration.debug) logger.debug("Registering route ${route::class.simpleName}")
		routes.add(route)
	}

	/**
	 * Internal method to install routes to the Ktor router
	 *
	 * @param routing Routing instance provided by Ktor
	 * */
	@ApiStatus.Internal
	fun installRoutes(routing: Routing) =
		routes.forEach { route ->
			routing.apply(route)
		}

	/**
	 * Registers Aster's built-in routes.
	 * */
	@ApiStatus.Internal
	fun registerBuiltinRoutes() {
		AdminDebugRoutes.register()
		AdminIndexRoutes.register()
		AdminInstanceRoutes.register()
		AdminInviteRoutes.register()
		AdminPluginRoutes.register()
		AdminQueueRoutes.register()
		AdminUserRoutes.register()

		ApNoteRoutes.register()
		ApUserRoutes.register()
		HostMetaRoutes.register()
		InboxRoutes.register()
		NodeInfoRoutes.register()
		OutboxRoutes.register()
		WebfingerRoutes.register()

		InviteRoutes.register()
		PolicyRoutes.register()

		DriveRoutes.register()
		FollowRequestRoutes.register()
		LoginRoutes.register()
		MetaRoutes.register()
		NoteRoutes.register()
		NotificationRoutes.register()
		PasswordResetRoutes.register()
		RegisterRoutes.register()
		SearchRoutes.register()
		TimelineRoutes.register()
		UserRoutes.register()

		FrontendRoutes.register()
		UploadRoutes.register()
	}
}
