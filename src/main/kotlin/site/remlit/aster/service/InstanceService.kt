package site.remlit.aster.service

import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.Instance
import site.remlit.aster.common.model.generated.PartialInstance
import site.remlit.aster.common.util.extractArray
import site.remlit.aster.common.util.extractObject
import site.remlit.aster.common.util.extractString
import site.remlit.aster.common.util.toLocalDateTime
import site.remlit.aster.db.entity.InstanceEntity
import site.remlit.aster.db.table.InstanceTable
import site.remlit.aster.event.instance.InstanceDiscoverEvent
import site.remlit.aster.event.instance.InstanceEditEvent
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.util.model.fromEntity
import kotlin.time.Clock

/**
 * Service for managing remote instances and resolving nodeinfo.
 *
 * @since 2025.11.1.0-SNAPSHOT
 * */
object InstanceService : Service {
	private val logger = LoggerFactory.getLogger(InstanceService::class.java)

	/**
	 * Reference for NodeInfo 2.0
	 * */
	const val NODEINFO_2_0: String = "http://nodeinfo.diaspora.software/ns/schema/2.0"

	/**
	 * Reference for NodeInfo 2.1
	 * */
	const val NODEINFO_2_1: String = "http://nodeinfo.diaspora.software/ns/schema/2.1"

	/**
	 * Currently being processed hosts
	 * */
	@JvmStatic
	var lockedHosts = mutableSetOf<String>()

	/**
	 * Get an instance entity
	 *
	 * @param where Query for finding instance
	 *
	 * @return Instance entity, if it exists
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): InstanceEntity? = transaction {
		InstanceEntity
			.find { where }
			.singleOrNull()
	}

	/**
	 * Get an instance entity by ID
	 *
	 * @param id ID of instance
	 *
	 * @return Instance entity, if it exists
	 * */
	@JvmStatic
	fun getById(id: String): InstanceEntity? = get(InstanceTable.id eq id)

	/**
	 * Get an instance entities
	 *
	 * @param where Query for finding instances
	 * @param take Number of instances to take
	 * @param offset Offset for query
	 *
	 * @return Instance entities, if they exist
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<InstanceEntity> = transaction {
		InstanceEntity
			.find { where }
			.offset(offset)
			.take(take)
			.toList()
	}

	/**
	 * Count instance entities
	 *
	 * @param where Query to find instances
	 *
	 * @return Count of instances where query applies
	 * */
	@JvmStatic
	fun count(where: Op<Boolean>): Long = transaction {
		InstanceEntity
			.find { where }
			.count()
	}

	/**
	 * Resolve an instance's nodeinfo and other identifying information
	 *
	 * @param host Host of instance
	 * @param refetch Whether to force a refetch
	 *
	 * @return Instance entity, if it exists or can be resolved
	 * */
	@JvmStatic
	suspend fun resolve(host: String, refetch: Boolean = false): InstanceEntity? {
		val existing = get(InstanceTable.host eq host)

		if ((existing != null) && !refetch) return existing

		val wellknownNodeinfoResponse = ResolverService.resolveSigned("https://$host/.well-known/nodeinfo")
			?: return null

		val links = extractArray { wellknownNodeinfoResponse["links"] } ?: return null
		var nodeinfoHref = ""

		for (link in links) {
			link as JsonObject

			val rel = extractString { link["rel"] } ?: continue
			val href = extractString { link["href"] } ?: continue

			if (rel == NODEINFO_2_0 && nodeinfoHref.isBlank() || rel == NODEINFO_2_1)
				nodeinfoHref = href
		}

		val resolvedNodeinfo = ResolverService.resolveSigned(nodeinfoHref)

		if (resolvedNodeinfo != null && existing == null)
			return register(toInstance(host, resolvedNodeinfo, existing))

		if (resolvedNodeinfo != null && existing != null)
			return update(toInstance(host, resolvedNodeinfo, existing))

		return null
	}

	/**
	 * Convert a resolved nodeinfo JsonObject into a PartialInstance
	 *
	 * @param host Host of the instance
	 * @param nodeinfo JsonObject of nodeinfo response
	 * @param existing Existing instance entity, if any
	 *
	 * @return Partial instance from provided information
	 * */
	@JvmStatic
	fun toInstance(host: String, nodeinfo: JsonObject, existing: InstanceEntity? = null): PartialInstance {
		val id = existing?.id?.toString() ?: IdentifierService.generate()

		val metadata = extractObject { nodeinfo["metadata"] }

		val name = extractString { metadata?.get("nodeName") }
		val description = extractString { metadata?.get("nodeDescription") }
		val color = extractString { metadata?.get("themeColor") }

		val maintainer = extractObject { metadata?.get("maintainer") }

		val contact = extractString { maintainer?.get("email") }

		val software = extractObject { nodeinfo["software"] }
		val softwareName = extractString { software?.get("name") }
		val version = extractString { software?.get("version") }

		return PartialInstance(
			id = id,
			host = host,

			name = name,
			description = description,
			color = color,
			icon = null,

			software = softwareName,
			version = version,
			contact = contact,

			createdAt = null,
			updatedAt = if (existing != null) Clock.System.now() else null,
		)
	}

	/**
	 * Updates an existing instance entity by a PartialInstance
	 *
	 * @param instance Partial instance
	 *
	 * @return Updated instance
	 * */
	@JvmStatic
	fun update(instance: PartialInstance): InstanceEntity? {
		try {
			transaction {
				InstanceEntity.findByIdAndUpdate(instance.id!!) {
					it.host = instance.host!!

					it.name = instance.name
					it.description = instance.description
					if (instance.color != null)
						it.color = instance.color!!
					it.icon = instance.icon

					it.software = instance.software
					it.version = instance.version
					it.contact = instance.contact

					it.updatedAt = instance.updatedAt?.toLocalDateTime()
				}
			}

			val instance = getById(instance.id!!)!!
			InstanceEditEvent(Instance.fromEntity(instance)).call()

			return instance
		} catch (_: Exception) {
			return null
		}
	}

	/**
	 * Registers a new instance entity by a PartialInstance
	 *
	 * @param instance Partial instance
	 *
	 * @return Created instance
	 * */
	@JvmStatic
	fun register(instance: PartialInstance): InstanceEntity? {
		try {
			transaction {
				InstanceEntity.new(instance.id) {
					this.host = instance.host!!

					this.name = instance.name
					this.description = instance.description
					if (instance.color != null)
						this.color = instance.color!!
					this.icon = instance.icon

					this.software = instance.software
					this.version = instance.version
					this.contact = instance.contact

					this.updatedAt = instance.updatedAt?.toLocalDateTime()
				}
			}

			val instance = getById(instance.id!!)!!
			InstanceDiscoverEvent(Instance.fromEntity(instance)).call()

			return instance
		} catch (_: Exception) {
			return null
		}
	}
}
