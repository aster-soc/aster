package site.remlit.aster.service.ap

import site.remlit.aster.model.config.Configuration
import site.remlit.aster.model.Service

/*
* Changing these can be bad!
*
* ActivityPub requires IDs to be constant, they cannot be changed!
* This service makes sure that the specific types of AP IDs generated
* across aster are consistent. Changing them can cause federation issues.
*
* Only modify this service if adding a new type of AP ID or adding extra
* functionality to an existing one that doesn't affect the previous
* use case.
* */

/**
 * Service to generate identifiers for ActivityPub objects.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object ApIdService : Service {
	@JvmStatic
	fun renderBaseApId(): String {
		return Configuration.url.toString()
	}

	/**
	 * @param id ID of a Note
	 */
	@JvmStatic
	fun renderNoteApId(id: String): String {
		return this.renderBaseApId() + "notes/" + id
	}

	/**
	 * @param id ID of a User
	 */
	@JvmStatic
	fun renderUserApId(id: String): String {
		return this.renderBaseApId() + "users/" + id
	}

	/**
	 * @param id ID of a User
	 */
	@JvmStatic
	fun renderInboxApId(id: String? = null): String {
		return if (!id.isNullOrEmpty()) this.renderBaseApId() + "users/" +
				id + "/inbox" else this.renderBaseApId() + "inbox"
	}


	/**
	 * @param id ID of a User
	 */
	@JvmStatic
	fun renderOutboxApId(id: String? = null): String {
		return if (!id.isNullOrEmpty()) this.renderBaseApId() + "users/" +
				id + "/outbox" else this.renderBaseApId() + "outbox"
	}

	/**
	 * @param id ID of a User
	 */
	@JvmStatic
	fun renderFollowingApId(id: String): String {
		return this.renderBaseApId() + "users/" + id + "/following"
	}

	/**
	 * @param id ID of a User
	 */
	@JvmStatic
	fun renderFollowersApId(id: String): String {
		return this.renderBaseApId() + "users/" + id + "/followers"
	}

	/**
	 * @param id ID of an activity
	 */
	@JvmStatic
	fun renderActivityApId(id: String): String {
		return this.renderBaseApId() + "activities/" + id
	}

	/**
	 * @param id ID of the relationship
	 */
	@JvmStatic
	fun renderFollowApId(id: String): String {
		return this.renderBaseApId() + "activities/" + id
	}

	/**
	 * @param id ID of the relationship
	 */
	@JvmStatic
	fun renderFollowAcceptApId(id: String): String {
		return this.renderBaseApId() + "activities/" + id + "/accept"
	}

	/**
	 * @param id ID of the relationship
	 */
	@JvmStatic
	fun renderFollowRejectApId(id: String): String {
		return this.renderBaseApId() + "activities/" + id + "/reject"
	}
}
