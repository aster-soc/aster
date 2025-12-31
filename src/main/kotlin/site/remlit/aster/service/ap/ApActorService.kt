package site.remlit.aster.service.ap

import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.generated.PartialUser
import site.remlit.aster.common.util.extractBoolean
import site.remlit.aster.common.util.extractObject
import site.remlit.aster.common.util.extractString
import site.remlit.aster.common.util.orNull
import site.remlit.aster.common.util.toLocalDateTime
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.model.Service
import site.remlit.aster.model.WellKnown
import site.remlit.aster.model.ap.ApImage
import site.remlit.aster.service.FormatService
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.service.InstanceService
import site.remlit.aster.service.ResolverService
import site.remlit.aster.service.SanitizerService
import site.remlit.aster.service.UserService
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.model.fromEntity
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Service to handle ActivityPub actors.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object ApActorService : Service {
	private val logger = LoggerFactory.getLogger(ApActorService::class.java)

	/**
	 * Resolve an actor by their ID
	 *
	 * @param apId ActivityPub ID of an actor
	 * @param refetch Force a refetch
	 *
	 * @return UserEntity or null
	 * */
	@JvmStatic
	suspend fun resolve(apId: String, refetch: Boolean = false): UserEntity? {
		val existing = UserService.getByApId(apId)

		if ((existing != null) && !refetch) return existing

		InstanceService.resolve(Url(apId).host)
		val resolveResponse = ResolverService.resolveSigned(apId)

		if (resolveResponse != null && existing == null)
			return register(toUser(resolveResponse) ?: return null)

		if (resolveResponse != null && existing != null)
			return update(toUser(resolveResponse, User.fromEntity(existing)) ?: return null)

		return null
	}

	/**
	 * Resolve an actor by their handle
	 *
	 * @param handle Handle starting with @, potentially also containing a host
	 *
	 * @return UserEntity or null
	 * */
	@JvmStatic
	suspend fun resolveHandle(handle: String): UserEntity? {
		val split = handle.split("@")

		val username = split.getOrNull(1) ?: return null
		val host = split.getOrNull(2) ?: return UserService.getByUsername(username)

		InstanceService.resolve(host)
		val webfingerResponse = jsonConfig.decodeFromString<WellKnown>(
			jsonConfig.encodeToString(
				ResolverService.resolveSigned("https://$host/.well-known/webfinger?resource=acct:@$username@$host")
			)
		)

		val apId = webfingerResponse.links?.firstOrNull { it.rel == "self" }?.href
			?: return null

		return resolve(apId)
	}

	// partials used here since a regular user has the expectation of being real,
	// may in future have calculated fields like likes on note, where creating them
	// would waste a query and potentially error
	@JvmStatic
	fun toUser(
		json: JsonObject,
		existing: User? = null
	): PartialUser? {
		val extractedId = extractString { json["id"] }

		if (extractedId.isNullOrBlank()) {
			logger.debug("Actor ID is null or blank")
			return null
		}

		val extractedType = extractString { json["type"] }

		if (
			extractedType.isNullOrBlank() ||
			!listOf("Person", "Service").contains(extractedType)
		) {
			logger.debug("Actor type is null, is blank, or invalid")
			return null
		}

		val extractedPreferredUsername = extractString { json["preferredUsername"] }

		if (extractedPreferredUsername.isNullOrBlank()) {
			logger.debug("Actor preferredUsername is null or blank")
			return null
		}

		val extractedSharedInbox = extractString { json["sharedInbox"] }
		val extractedInbox = extractString { json["inbox"] }

		val inbox = extractedSharedInbox ?: extractedInbox

		if (inbox == null || inbox.isBlank()) {
			logger.debug("Actor inbox is null or blank")
			return null
		}

		val extractedMisskeySummary = extractString { json["_misskey_summary"] }
		val extractedSummary = extractString { json["summary"] }

		val summary = extractedMisskeySummary ?: extractedSummary

		val extractedIcon = json["icon"]
		val extractedImage = json["image"]

		val icon = if (extractedIcon is JsonObject)
			jsonConfig.decodeFromJsonElement<ApImage>(extractedIcon) else null
		val image = if (extractedImage is JsonObject)
			jsonConfig.decodeFromJsonElement<ApImage>(extractedImage) else null

		val extractedPublicKey = extractString {
			extractObject { json["publicKey"] }?.get("publicKeyPem")
		}

		if (extractedPublicKey.isNullOrBlank()) {
			logger.debug("Actor public key is null or blank")
			return null
		}

		val followers = extractString { json["followers"] }
		val following = extractString { json["following"] }

		val extractedPublished = extractString { json["published"] }
		val published = if (extractedPublished != null)
			orNull { Instant.parse(extractedPublished) } ?: Clock.System.now()
		else Clock.System.now()

		return PartialUser(
			id = existing?.id ?: IdentifierService.generate(),
			apId = existing?.apId ?: extractedId,

			username = SanitizerService.sanitize(extractedPreferredUsername, true),
			displayName = extractString { json["name"] },

			host = existing?.host ?: FormatService.toASCII(Url(extractedId).host),

			bio = if (summary != null) SanitizerService.sanitize(summary) else null,
			birthday = null,
			location = null,

			avatar = icon?.url,
			avatarAlt = icon?.summary ?: icon?.name,
			banner = image?.url,
			bannerAlt = image?.summary ?: image?.name,

			inbox = inbox,
			outbox = extractString { json["outbox"] },

			activated = true,
			automated = extractedType != "Person",
			suspended = existing?.suspended ?: false,
			sensitive = extractBoolean { json["sensitive"] } ?: false,
			discoverable = extractBoolean { json["discoverable"] } ?: false,
			locked = extractBoolean { json["manuallyApprovesFollowers"] } ?: false,
			indexable = extractBoolean { json["noindex"] }?.let { !it } ?: true,
			isCat = extractBoolean { json["isCat"] } ?: false,
			speakAsCat = extractBoolean { json["speakAsCat"] } ?: false,

			followersUrl = followers,
			followingUrl = following,

			createdAt = published,
			updatedAt = if (existing != null) Clock.System.now() else null,

			publicKey = existing?.publicKey ?: extractedPublicKey,
		)
	}

	/**
	 * Update an existing actor
	 *
	 * @param user Converted actor to partial user
	 *
	 * @return UserEntity or null
	 * */
	@JvmStatic
	fun update(user: PartialUser): UserEntity? {
		try {
			transaction {
				UserEntity.findByIdAndUpdate(user.id!!) {
					it.apId = user.apId!!

					it.username = user.username!!
					it.displayName = user.displayName

					it.host = user.host

					it.bio = user.bio
					it.birthday = user.birthday
					it.location = user.location

					it.avatar = user.avatar
					it.avatarAlt = user.avatarAlt
					it.banner = user.banner
					it.bannerAlt = user.bannerAlt

					it.inbox = user.inbox!!
					it.outbox = user.outbox

					it.activated = user.activated ?: true
					it.automated = user.automated ?: false
					it.suspended = user.suspended ?: false
					it.sensitive = user.sensitive ?: false
					it.discoverable = user.discoverable ?: false
					it.locked = user.locked ?: false
					it.indexable = user.indexable ?: false
					it.isCat = user.isCat ?: false
					it.speakAsCat = user.speakAsCat ?: false

					it.followersUrl = user.followersUrl
					it.followingUrl = user.followingUrl

					it.createdAt = (user.createdAt ?: Clock.System.now()).toLocalDateTime()
					it.updatedAt = user.updatedAt?.toLocalDateTime()

					it.publicKey = user.publicKey!!
				}
			}

			return UserService.getById(user.id!!)
		} catch (e: Exception) {
			logger.error(e.message, e)
			return null
		}
	}

	/**
	 * Register a new actor
	 *
	 * @param user Converted actor to partial user
	 *
	 * @return UserEntity or null
	 * */
	@JvmStatic
	fun register(user: PartialUser): UserEntity? {
		try {
			transaction {
				UserEntity.new(user.id) {
					this.apId = user.apId!!

					this.username = user.username!!
					this.displayName = user.displayName

					this.host = user.host

					this.bio = user.bio
					this.birthday = user.birthday
					this.location = user.location

					this.avatar = user.avatar
					this.avatarAlt = user.avatarAlt
					this.banner = user.banner
					this.bannerAlt = user.bannerAlt

					this.inbox = user.inbox!!
					this.outbox = user.outbox

					this.activated = user.activated ?: true
					this.automated = user.automated ?: false
					this.suspended = user.suspended ?: false
					this.sensitive = user.sensitive ?: false
					this.discoverable = user.discoverable ?: false
					this.locked = user.locked ?: false
					this.indexable = user.indexable ?: false
					this.isCat = user.isCat ?: false
					this.speakAsCat = user.speakAsCat ?: false

					this.followersUrl = user.followersUrl
					this.followingUrl = user.followingUrl

					this.createdAt = (user.createdAt ?: Clock.System.now()).toLocalDateTime()
					this.updatedAt = user.updatedAt?.toLocalDateTime()

					this.publicKey = user.publicKey!!
				}
			}

			return UserService.getById(user.id!!)
		} catch (e: Exception) {
			logger.error(e.message, e)
			return null
		}
	}
}
