package site.remlit.aster.service

import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.model.Service
import site.remlit.aster.util.model.fromEntity

/**
 * Service for handling visibility in non-ActivityPub contexts.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object VisibilityService : Service {
	/**
	 * Determines if a user can see an entity
	 *
	 * @param visibility Visibility of the entity
	 * @param author Author of the entity
	 * @param to List of user ids, other users who can see this entity
	 *           (for direct visibility). Not applicable to all entities.
	 * @param user User who is trying to view the entity
	 * @param ignoreBlock Whether to take block relationships into account
	 *
	 * @return If the user can see the entity
	 * */
	@JvmStatic
	fun canISee(
		visibility: Visibility,
		author: String,
		to: List<String>? = null,
		user: String,
		ignoreBlock: Boolean = false
	): Boolean {
		val author = User.fromEntity(
			UserService.getById(author)
				?: throw IllegalArgumentException("Author not found")
		)
		val user = User.fromEntity(
			UserService.getById(user)
				?: throw IllegalArgumentException("User not found")
		)

		var ignoreBlock = ignoreBlock

		// if neither are local, we don't have to take blocks into account
		if (!author.isLocal() && !user.isLocal())
			ignoreBlock = true

		if (!ignoreBlock && RelationshipService.eitherBlocking(user.id, author.id))
			return false

		if (to != null && to.contains(user.id))
			return true

		return when (visibility) {
			Visibility.Public -> true
			Visibility.Unlisted -> true
			Visibility.Followers -> false
			Visibility.Direct -> false
		}
	}
}
