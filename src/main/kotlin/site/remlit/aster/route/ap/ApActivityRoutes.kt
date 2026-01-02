package site.remlit.aster.route.ap

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import site.remlit.aster.db.table.RelationshipTable
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.activity.ApAcceptActivity
import site.remlit.aster.model.ap.activity.ApFollowActivity
import site.remlit.aster.model.ap.activity.ApRejectActivity
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.RelationshipService
import site.remlit.aster.service.ap.ApIdService

object ApActivityRoutes {
	fun register() = RouteRegistry.registerRoute {
		get("/activities/{id}") {
			val id = call.parameters.getOrFail("id")

			val relationship = RelationshipService.getById(id)

			if (relationship != null && relationship.from.isLocal() && !relationship.to.isLocal())
				return@get call.respond(
					HttpStatusCode.OK,
					ApFollowActivity(
						ApIdService.renderFollowApId(relationship.id),
						actor = relationship.from.apId,
						`object` = ApIdOrObject.Id(relationship.to.apId)
					)
				)

			throw ApiException(HttpStatusCode.NotFound)
		}

		get("/activities/{id}/accept") {
			val id = call.parameters.getOrFail("id")

			val relationship = RelationshipService.getById(id)

			if (
				relationship == null ||
				relationship.activityId == null ||
				!relationship.to.isLocal() ||
				relationship.from.isLocal()
			) throw ApiException(HttpStatusCode.NotFound)

			return@get call.respond(
				HttpStatusCode.OK,
				ApAcceptActivity(
					ApIdService.renderFollowApId(relationship.id),
					actor = relationship.to.apId,
					`object` = ApIdOrObject.Id(relationship.activityId!!)
				)
			)
		}

		get("/activities/{id}/reject") {
			val id = call.parameters.getOrFail("id")

			val relationship = RelationshipService.getById(id)

			if (
				relationship == null ||
				relationship.activityId == null ||
				!relationship.to.isLocal() ||
				relationship.from.isLocal()
			) throw ApiException(HttpStatusCode.NotFound)

			return@get call.respond(
				HttpStatusCode.OK,
				ApRejectActivity(
					ApIdService.renderFollowApId(relationship.id),
					actor = relationship.to.apId,
					`object` = ApIdOrObject.Id(relationship.activityId!!)
				)
			)
		}
	}
}
