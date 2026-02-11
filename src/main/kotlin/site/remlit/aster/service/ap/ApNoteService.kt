package site.remlit.aster.service.ap

import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.NoteAttachment
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.common.model.generated.PartialNote
import site.remlit.aster.common.util.extractString
import site.remlit.aster.common.util.ifFails
import site.remlit.aster.common.util.orNull
import site.remlit.aster.common.util.toLocalDateTime
import site.remlit.aster.db.entity.NoteEntity
import site.remlit.aster.event.note.NoteCreateEvent
import site.remlit.aster.event.note.NoteEditEvent
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.model.ap.ApDocument
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.service.DriveService
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.service.InstanceService
import site.remlit.aster.service.NoteService
import site.remlit.aster.service.ResolverService
import site.remlit.aster.service.UserService
import site.remlit.aster.service.VisibilityService
import site.remlit.aster.util.detached
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
		val existingNote = NoteService.getByApId(apId)

		if ((existingNote != null) && !refetch) {
			return existingNote
		}

		CoroutineScope(Dispatchers.Default).launch { InstanceService.resolve(Url(apId).host) }
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

		val to = jsonConfig.decodeFromJsonElement<List<String>>(json["to"] ?: return null)
		val cc = jsonConfig.decodeFromJsonElement<List<String>>(json["cc"] ?: return null)

		val determinedVisibility = ApVisibilityService.determineVisibility(
			to,
			cc,
			author.followersUrl,
			extractString { json["visibility"] }
		)

		val inReplyTo = extractString { json["inReplyTo"] }
		val replyingTo = if (!inReplyTo.isNullOrBlank())
			orNull {
				val resolved = resolve(inReplyTo, depth = depth + 1)
					?: return@orNull null

				if (!VisibilityService.canISee(
						resolved.visibility,
						resolved.user.id,
						resolved.to,
						author.id.toString()
					)) return@orNull null

				if (resolved.visibility > determinedVisibility)
					return@orNull null

				return@orNull resolved
			}
		else null

		val summary = extractString { json["summary"] }
		val misskeySummary = extractString { json["_misskey_summary"] }

		val content = extractString { json["content"] }
		val misskeyContent = extractString { json["_misskey_content"] }

		val extractedAttachment = json["attachment"]
		val documents = if (extractedAttachment != null)
			jsonConfig.decodeFromJsonElement<List<ApDocument>>(extractedAttachment)
		else null

		val attachments = mutableListOf<NoteAttachment>()

		documents?.forEach { document ->
			val file = DriveService.create(
				author,
				ifFails({
					ContentType.parse(document.mediaType ?: "application/octet-stream")
				}) {
					ContentType.Application.OctetStream
				},
				Url(document.url),
				document.summary ?: document.name
			)

			attachments.add(NoteAttachment(
				file.id,
				file.src,
				file.alt,
				file.type,
			))
		}

		val extractedPublished = extractString { json["published"] }
		val published = if (extractedPublished != null)
			orNull { Instant.parse(extractedPublished) } ?: Clock.System.now()
		else Clock.System.now()

		val finalSummary = misskeySummary ?: summary
		val finalContent = misskeyContent ?: content

		val id = existing?.id ?: IdentifierService.generate()

		val replies = json["replies"]
		if (replies != null)
			coroutineScope {
				launch {
					when (val decodedReplies = jsonConfig.decodeFromJsonElement<ApIdOrObject>(replies)) {
						is ApIdOrObject.Id ->
							ApBackfillService.getReplies(decodedReplies.value)
						is ApIdOrObject.Object -> {
							val id = extractString { decodedReplies.value.jsonObject["id"] } ?: return@launch
							ApBackfillService.getReplies(id)
						}
					}
				}
			}

		return PartialNote(
			id = id,
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
			attachments = attachments
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

					it.replyingTo = if (note.replyingTo != null)
						transaction { NoteEntity[note.replyingTo!!.id] }
					else null
					it.attachments = note.attachments?.map { a -> a.id } ?: emptyList()

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

					this.replyingTo = if (note.replyingTo != null)
						transaction { NoteEntity[note.replyingTo!!.id] }
					else null
					this.attachments = note.attachments?.map { a -> a.id } ?: emptyList()

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
