package site.remlit.aster.service.ap

import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.common.model.generated.PartialNote
import site.remlit.aster.common.util.extractString
import site.remlit.aster.common.util.orNull
import site.remlit.aster.common.util.toLocalDateTime
import site.remlit.aster.db.entity.NoteEntity
import site.remlit.aster.event.note.NoteCreateEvent
import site.remlit.aster.event.note.NoteEditEvent
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.service.InstanceService
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.ResolverService
import site.remlit.aster.service.TimeService
import site.remlit.aster.service.UserService
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.model.fromEntity
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Service to handle ActivityPub notes.
 *
 * @since 2025.10.5.0-SNAPSHOT
 * */
object ApNoteService : Service {
	private val logger = LoggerFactory.getLogger(ApNoteService::class.java)

	/**
	 * Resolve a note by its ID
	 *
	 * @param apId ActivityPub ID of a note
	 * @param refetch Refetch actor
	 * @param depth Maximum recursion depth
	 * @param user User to resolve as
	 *
	 * @return Note or null
	 * */
	@JvmStatic
	suspend fun resolve(
		apId: String,
		refetch: Boolean = false,
		depth: Int = 0,
		user: String? = null
	): Note? {
		if (depth > Configuration.maxResolveDepth) return null

		InstanceService.resolve(Url(apId).host)
		val existingNote = NoteService.getByApId(apId)

		if ((existingNote != null) && !refetch) {
			return existingNote
		}

		val resolveResponse = ResolverService.resolveSigned(apId, user = user)

		if (resolveResponse != null && existingNote == null)
			return register(toNote(resolveResponse, depth = depth + 1) ?: return null)

		if (resolveResponse != null && existingNote != null)
			return update(toNote(resolveResponse, existingNote, depth = depth + 1) ?: return null)

		return null
	}

	/**
	 * Converts an ActivityPub note to a PartialNote
	 *
	 * @param json JSON object
	 * @param existing Existing note
	 *
	 * @return PartialNote or null
	 */
	@JvmStatic
	suspend fun toNote(json: JsonObject, existing: Note? = null, depth: Int = 0): PartialNote? {
		val apId = extractString { json["id"] }
		if (apId.isNullOrBlank()) return null

		val type = extractString { json["type"] }
		if (type.isNullOrBlank() || type != "Note") return null

		val attributedTo = extractString { json["attributedTo"] }
		if (attributedTo.isNullOrBlank()) return null

		val author = ApActorService.resolve(attributedTo) ?: return null

		// todo: maximum depth, otherwise this gets messy fast
		val inReplyTo = extractString { json["inReplyTo"] }
		val replyingTo = if (inReplyTo.isNullOrBlank())
			null
		else orNull { resolve(inReplyTo, depth = depth + 1) }

		val summary = extractString { json["summary"] }
		val misskeySummary = extractString { json["_misskey_summary"] }

		val content = extractString { json["content"] }
		val misskeyContent = extractString { json["_misskey_content"] }

		val extractedPublished = extractString { json["published"] }
		val published = if (extractedPublished != null)
			orNull { Instant.parse(extractedPublished) } ?: Clock.System.now()
		else Clock.System.now()

		val to = jsonConfig.decodeFromJsonElement<List<String>>(json["to"] ?: return null)
		val cc = jsonConfig.decodeFromJsonElement<List<String>>(json["cc"] ?: return null)

		val determinedVisibility =
			ApVisibilityService.determineVisibility(
				to,
				cc,
				author.followersUrl,
				extractString { json["visibility"] }
			)

		val finalSummary = misskeySummary ?: summary
		val finalContent = misskeyContent ?: content

		return PartialNote(
			id = existing?.id ?: IdentifierService.generate(),
			apId = existing?.apId ?: apId,
			user = User.fromEntity(author),
			conversation = null,

			cw = finalSummary,
			content = finalContent,
			visibility = determinedVisibility,
			tags = null,
			to = null,

			replyingTo = replyingTo,
			repeat = existing?.repeat,

			likes = existing?.likes,
			reactions = existing?.reactions,
			repeats = existing?.repeats,

			createdAt = published,
			updatedAt = if (existing != null) Clock.System.now() else null,
		)
	}

	/**
	 * Update a note
	 *
	 * @param note Converted note to partial note
	 *
	 * @return Note or null
	 * */
	@JvmStatic
	fun update(note: PartialNote): Note? {
		try {
			transaction {
				NoteEntity.findByIdAndUpdate(note.id!!) {
					it.apId = note.apId!!
					it.user = UserService.getById(note.user?.id!!)!!
					it.cw = note.cw
					// todo: note nullability
					it.content = note.content.orEmpty()
					it.visibility = note.visibility ?: Visibility.Direct

					it.replyingTo =
						if (note.replyingTo != null) transaction { NoteEntity[note.replyingTo!!.id] } else null

					// todo: to
					it.to = note.to.orEmpty()
					// todo: tags
					// todo: emojis
					// todo: repeat

					it.createdAt = note.createdAt!!.toLocalDateTime()
					it.updatedAt = note.updatedAt?.toLocalDateTime()
				}
			}

			val newNote = NoteService.getById(note.id!!) ?: return null

			NoteEditEvent(newNote).call()

			return newNote
		} catch (e: Exception) {
			logger.error(e.message, e)
			return null
		}
	}

	/**
	 * Register a new note
	 *
	 * @param note Converted note to partial note
	 *
	 * @return Note or null
	 * */
	@JvmStatic
	fun register(note: PartialNote): Note? {
		try {
			transaction {
				NoteEntity.new(note.id!!) {
					this.apId = note.apId!!
					this.user = UserService.getById(note.user?.id!!)!!
					this.cw = note.cw
					// todo: note nullability
					this.content = note.content.orEmpty()
					this.visibility = note.visibility ?: Visibility.Direct

					this.replyingTo =
						if (note.replyingTo != null) transaction { NoteEntity[note.replyingTo!!.id] } else null

					// todo: to
					this.to = note.to.orEmpty()
					// todo: tags
					// todo: emojis
					// todo: repeat

					this.createdAt = note.createdAt!!.toLocalDateTime()
				}
			}

			val note = NoteService.getById(note.id!!) ?: return null

			NoteCreateEvent(note).call()

			return note
		} catch (e: Exception) {
			logger.error(e.message, e)
			return null
		}
	}
}
