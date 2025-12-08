package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.Relationship
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.type.NotificationType
import site.remlit.aster.common.model.type.RelationshipType
import site.remlit.aster.db.entity.RelationshipEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.RelationshipTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.event.user.UserFollowEvent
import site.remlit.aster.event.user.UserFollowRequestEvent
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.activity.ApAcceptActivity
import site.remlit.aster.model.ap.activity.ApFollowActivity
import site.remlit.aster.service.ap.ApDeliverService
import site.remlit.aster.service.ap.ApIdService
import site.remlit.aster.util.model.fromEntities
import site.remlit.aster.util.model.fromEntity

/**
 * Service for managing user relationships.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object RelationshipService : Service {
	/**
	 * Reference the "to" user on a relationship.
	 * For usage in queries.
	 * */
	@JvmStatic
	val userToAlias = UserTable.alias("to")

	/**
	 * Reference the "from" user on a relationship.
	 * For usage in queries.
	 * */
	@JvmStatic
	val userFromAlias = UserTable.alias("from")

	/**
	 * Get a relationship.
	 *
	 * @param where Query to find relationship
	 *
	 * @return Relationship, if any
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): Relationship? = transaction {
		val relationship = RelationshipTable
			.join(userToAlias, JoinType.INNER, RelationshipTable.to, userToAlias[UserTable.id])
			.join(userFromAlias, JoinType.INNER, RelationshipTable.from, userFromAlias[UserTable.id])
			.selectAll()
			.where { where }
			.let { RelationshipEntity.wrapRows(it) }
			.singleOrNull()

		if (relationship != null)
			Relationship.fromEntity(relationship)
		else null
	}

	/**
	 * Get a relationship between two users
	 *
	 * @param id Relationship ID
	 *
	 * @return Relationship, if any
	 * */
	@JvmStatic
	fun getById(id: String) = get(RelationshipTable.id eq id)

	/**
	 * Get a relationship between two users
	 *
	 * @param to Relationship target
	 * @param from Relationship owner
	 *
	 * @return Relationship, if any
	 * */
	@JvmStatic
	fun getByIds(to: String, from: String) = get(
		userToAlias[UserTable.id] eq to
				and (userFromAlias[UserTable.id] eq from)
	)

	/**
	 * Get many relationships
	 *
	 * @param where Query to find relationships
	 * @param take Number of relationships to take
	 * @param offset Offset for query
	 *
	 * @return Relationships, if any
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<Relationship> = transaction {
		val entities = RelationshipTable
			.join(userToAlias, JoinType.INNER, RelationshipTable.to, userToAlias[UserTable.id])
			.join(userFromAlias, JoinType.INNER, RelationshipTable.from, userFromAlias[UserTable.id])
			.selectAll()
			.where { where }
			.offset(offset)
			.let { RelationshipEntity.wrapRows(it) }
			.sortedByDescending { it.createdAt }
			.take(take)
			.toList()

		if (!entities.isEmpty())
			Relationship.fromEntities(entities)
		else listOf()
	}

	/**
	 * Gets ID of activity used to create a relationship
	 *
	 * @param relationshipId ID of the relationship
	 *
	 * @return ActivityPub ID of the activity used to create the relationship
	 * */
	@JvmStatic
	fun getActivityId(relationshipId: String): String? = transaction {
		RelationshipEntity
			.find { RelationshipTable.id eq relationshipId }
			.singleOrNull()
			?.activityId
	}

	/**
	 * Get followers of a user
	 *
	 * @param user Query to find relationships
	 *
	 * @return Following users
	 * */
	@JvmStatic
	fun getFollowers(user: UserEntity): List<User> =
		getMany(userToAlias[UserTable.id] eq user.id).map { it.from }

	/**
	 * Get following of a user
	 *
	 * @param user Query to find relationships
	 *
	 * @return Followed users
	 * */
	@JvmStatic
	fun getFollowing(user: UserEntity): List<User> =
		getMany(userFromAlias[UserTable.id] eq user.id).map { it.to }

	/**
	 * Get relationships in both directions for two users
	 *
	 * @param to Relationship target
	 * @param from Relationship owner
	 *
	 * @return Pair of Relationship, where first is to and second is from
	 * */
	@JvmStatic
	fun getPair(to: String, from: String): Pair<Relationship?, Relationship?> {
		return Pair(
			this.get(RelationshipTable.to eq from and (RelationshipTable.from eq to)),
			this.get(RelationshipTable.to eq to and (RelationshipTable.from eq from))
		)
	}

	@JvmStatic
	fun mapPair(pair: Pair<Relationship?, Relationship?>): Map<String, Relationship?> {
		return mapOf(
			"to" to pair.first,
			"from" to pair.second
		)
	}

	//<editor-fold desc="Checks">
	/**
	 * Determine if there is a block relationship in either direction
	 *
	 * @param to First user
	 * @param from Second user
	 *
	 * @return If either are blocking each other
	 * */
	@JvmStatic
	fun eitherBlocking(to: String, from: String): Boolean {
		val pair = this.getPair(to, from)

		if (pair.first != null && pair.first?.type == RelationshipType.Block)
			return true

		if (pair.second != null && pair.first?.type == RelationshipType.Block)
			return true

		return false
	}

	/**
	 * Determine if there is a mute relationship in one direction
	 *
	 * @param to Relationship target
	 * @param from Relationship owner
	 *
	 * @return If `from` is muting `to`
	 * */
	@JvmStatic
	fun muteExists(to: String, from: String): Boolean {
		val relationship = this.get(RelationshipTable.to eq to and (RelationshipTable.from eq from))

		return relationship != null && relationship.type == RelationshipType.Mute
	}
	//</editor-fold>

	//<editor-fold desc="Creation">
	/**
	 * Follow a user as another
	 *
	 * @param to Relationship target
	 * @param from Relationship owner
	 * @param followId ID of Follow activity
	 *
	 * @return Relationship pair
	 * */
	@JvmStatic
	fun follow(to: String, from: String, followId: String? = null): Pair<Relationship?, Relationship?> {
		if (eitherBlocking(to, from) || to == from)
			throw IllegalArgumentException("You cannot follow this user")

		val existing = getByIds(to, from)

		if (existing != null)
			throw IllegalArgumentException("You have an existing relationship with this user")

		val to = UserService.getById(to) ?: throw IllegalArgumentException("Target not found")
		val from = UserService.getById(from) ?: throw IllegalArgumentException("Sender not found")

		val activityId = ApIdService.renderActivityApId(IdentifierService.generate())

		if (to.host != null)
			ApDeliverService.deliver(
				ApFollowActivity(
					activityId,
					actor = from.apId,
					`object` = ApIdOrObject.Id(to.apId)
				),
				from,
				to.inbox
			)

		val id = IdentifierService.generate()

		transaction {
			RelationshipEntity.new(id) {
				this.type = RelationshipType.Follow
				this.to = to
				this.from = from
				this.pending = to.locked || to.host != null
				this.activityId = if (to.host != null && followId == null) activityId else followId
			}
		}

		val relationship = getByIds(to.id.toString(), from.id.toString())
			?: throw IllegalArgumentException("Relationship not found")

		if (to.host == null) {
			NotificationService.create(
				NotificationType.Follow,
				to,
				from,
				relationship
			)

			if (to.locked) UserFollowRequestEvent(relationship, User.fromEntity(to)) else {
				UserFollowEvent(relationship, User.fromEntity(to))

				if (followId != null) {
					ApDeliverService.deliver<ApAcceptActivity>(
						ApAcceptActivity(
							ApIdService.renderActivityApId(IdentifierService.generate()),
							actor = to.apId,
							`object` = ApIdOrObject.Id(activityId)
						),
						to,
						from.inbox
					)
				}
			}
		}

		return getPair(to.id.toString(), from.id.toString())
	}

	/**
	 * Unfollow a user as another
	 *
	 * @param to Relationship target
	 * @param from Relationship owner
	 * */
	@JvmStatic
	fun unfollow(to: String, from: String): Nothing = TODO()

	/**
	 * Accept a follow
	 *
	 * @param id ID of relationship
	 *
	 * @return Updated relationship
	 * */
	@JvmStatic
	fun accept(id: String): Relationship? {
		// todo: Notifications
		transaction {
			RelationshipEntity.findByIdAndUpdate(id) {
				it.pending = false
			}
		}
		return getById(id)
	}

	/**
	 * Accept a follow
	 *
	 * @param apId Follow Activity ID
	 *
	 * @return Updated relationship
	 * */
	@JvmStatic
	fun acceptByApId(apId: String): Relationship? = accept(
		get(RelationshipTable.activityId eq apId)?.id
			?: throw IllegalArgumentException("Relationship not found")
	)
	//</editor-fold>
}
