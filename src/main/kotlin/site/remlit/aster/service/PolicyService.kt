package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.Policy
import site.remlit.aster.common.model.type.PolicyType
import site.remlit.aster.db.entity.PolicyEntity
import site.remlit.aster.db.table.PolicyTable
import site.remlit.aster.event.policy.PolicyCreateEvent
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.util.model.fromEntity

/**
 * Service for managing federation policies.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object PolicyService : Service {
	/**
	 * Get a policy.
	 *
	 * @param where Query to find policy
	 *
	 * @return Found policy, if any
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): PolicyEntity? = transaction {
		PolicyEntity
			.find { where }
			.singleOrNull()
	}

	/**
	 * Get a policy by ID.
	 *
	 * @param id ID of policy
	 *
	 * @return Found policy, if any
	 * */
	@JvmStatic
	fun getById(id: String): PolicyEntity? = get(PolicyTable.id eq id)

	/**
	 * Get many policies.
	 *
	 * @param where Query to find policies
	 * @param take Number of policies to take
	 * @param offset Offset for query
	 *
	 * @return Policies, if any
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<PolicyEntity> = transaction {
		PolicyEntity
			.find { where }
			.offset(offset)
			.sortedByDescending { it.createdAt }
			.take(take)
			.toList()
	}

	/**
	 * Get many policies by type.
	 *
	 * @param type Type of policy to find
	 *
	 * @return Found policies, if any
	 * */
	@JvmStatic
	fun getAllByType(type: PolicyType): List<PolicyEntity> = transaction {
		PolicyEntity
			.find { PolicyTable.type eq type }
			.sortedByDescending { it.createdAt }
			.toList()
	}

	/**
	 * Reduce a list of policies to their hosts
	 *
	 * @param policies List of policies to reduce
	 *
	 * @return List of hosts
	 * */
	@JvmStatic
	fun reducePoliciesToHost(policies: List<PolicyEntity>): List<String> {
		val reducedHosts: MutableList<String> = mutableListOf()

		for (policy in policies) {
			reducedHosts.add(policy.host)
		}

		return reducedHosts
	}

	/**
	 * Create a new federation policy
	 *
	 * @param type Type of policy
	 * @param host Host policy is applied to
	 * @param content Content, if applicable
	 *
	 * @return Created policy
	 * */
	@JvmStatic
	fun create(
		type: PolicyType,
		host: String,
		content: String? = null,
	): PolicyEntity {
		val id = IdentifierService.generate()

		transaction {
			PolicyEntity.new(id) {
				this.type = type
				this.host = host
				this.content = content
			}
		}

        val policy = getById(id)!!
        PolicyCreateEvent(Policy.fromEntity(policy)).call()

		return policy
	}
}
