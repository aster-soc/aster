package site.remlit.aster.route.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.Policy
import site.remlit.aster.common.model.type.PolicyType
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.db.entity.PolicyEntity
import site.remlit.aster.db.table.PolicyTable
import site.remlit.aster.model.ApiException
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.service.PolicyService
import site.remlit.aster.service.TimelineService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.model.fromEntities
import site.remlit.aster.util.model.fromEntity

internal object PolicyRoutes {
	@Serializable
	data class PolicyBody(
		val type: PolicyType,
		val host: String,
		val content: String? = null
	)

	fun register() =
		// todo: some of this needs to be moved into the service
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Mod
			) {
				get("/api/mod/policies") {
					val since = TimelineService.normalizeSince(call.parameters["since"])
					val take = TimelineService.normalizeTake(call.parameters["take"]?.toIntOrNull())

					val policies = PolicyService.getMany(PolicyTable.createdAt less since, take)

					if (policies.isEmpty()) {
						call.respond(HttpStatusCode.NoContent)
						return@get
					}

					call.respond(Policy.fromEntities(policies))
				}

				post("/api/mod/policy") {
					val body = call.receive<PolicyBody>()

					if (
						(body.type == PolicyType.ForceContentWarning) && body.content == null
					)
						throw ApiException(HttpStatusCode.BadRequest, "This policy type requires content")

					val id = IdentifierService.generate()

					transaction {
						PolicyEntity.new(id) {
							type = body.type
							host = body.host
							content = body.content
						}
					}

					val policy = PolicyService.getById(id) ?: throw ApiException(
						HttpStatusCode.NotFound,
						"Policy doesn't exist"
					)

					call.respond(Policy.fromEntity(policy))
				}

				patch("/api/mod/policy/{id}") {
					val policy =
						PolicyService.getById(call.parameters.getOrFail("id"))
							?: throw ApiException(HttpStatusCode.NotFound)

					throw ApiException(HttpStatusCode.NotImplemented)
				}

				delete("/api/mod/policy/{id}") {
					val policy =
						PolicyService.getById(call.parameters.getOrFail("id"))
							?: throw ApiException(HttpStatusCode.NotFound)

					transaction {
						policy.delete()
					}

					call.respond(HttpStatusCode.OK)
				}
			}
		}
}
