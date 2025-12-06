package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.SmallNote
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.common.model.type.NotificationType
import site.remlit.aster.db.entity.NoteEntity
import site.remlit.aster.db.entity.NoteLikeEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.NoteLikeTable
import site.remlit.aster.db.table.NoteTable
import site.remlit.aster.db.table.NotificationTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.event.note.NoteCreateEvent
import site.remlit.aster.event.note.NoteDeleteEvent
import site.remlit.aster.event.note.NoteLikeEvent
import site.remlit.aster.event.note.NoteRepeatEvent
import site.remlit.aster.event.note.NoteUnlikeEvent
import site.remlit.aster.exception.InsertFailureException
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.activity.ApAnnounceActivity
import site.remlit.aster.model.ap.activity.ApLikeActivity
import site.remlit.aster.model.ap.activity.ApUndoActivity
import site.remlit.aster.service.ap.ApDeliverService
import site.remlit.aster.service.ap.ApIdService
import site.remlit.aster.service.ap.ApVisibilityService
import site.remlit.aster.util.model.fromEntities
import site.remlit.aster.util.model.fromEntity
import site.remlit.aster.util.sanitizeOrNull

/**
 * Service for managing notes.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object NoteService : Service {
	private val logger: Logger = LoggerFactory.getLogger(PluginService::class.java)

	/**
	 * Reference the "replyingTo" note on a note.
	 * For usage in queries.
	 * */
	@JvmStatic
	val replyingToAlias = NoteTable.alias("replyingTo")

	/**
	 * Reference the "repeat" note on a note.
	 * For usage in queries.
	 * */
	@JvmStatic
	val repeatAlias = NoteTable.alias("repeat")

	/**
	 * Get a note
	 *
	 * @param where Query to find note
	 *
	 * @return Note, if exists
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): Note? = transaction {
		val note = NoteEntity
			.find { where }
			.singleOrNull()
			?.load(NoteEntity::user, NoteEntity::replyingTo, NoteEntity::repeat)

		if (note != null)
			Note.fromEntity(note)
		else null
	}

	/**
	 * Get a note by ID
	 *
	 * @param id ID of note
	 *
	 * @return Note, if exists
	 * */
	@JvmStatic
	fun getById(id: String): Note? = get(NoteTable.id eq id)

	/**
	 * Get a note by ActivityPub ID
	 *
	 * @param apId ActivityPub ID of note
	 *
	 * @return Note, if exists
	 * */
	@JvmStatic
	fun getByApId(apId: String): Note? = get(NoteTable.apId eq apId)

	/**
	 * Get many notes
	 *
	 * @param where Query to find notes
	 * @param take Number of notes to take
	 * @param offset Offset for query
	 *
	 * @return Notes, if exist
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<Note> = transaction {
		val notes = (NoteTable innerJoin UserTable)
			.join(replyingToAlias, JoinType.LEFT, NoteTable.replyingTo, replyingToAlias[NoteTable.id])
			.join(repeatAlias, JoinType.LEFT, NoteTable.repeat, repeatAlias[NoteTable.id])
			.selectAll()
			.where { where }
			.offset(offset)
			.let { NoteEntity.wrapRows(it) }
			.sortedByDescending { it.createdAt }
			.take(take)
			.toList()

		if (!notes.isEmpty())
			Note.fromEntities(notes)
		else listOf()
	}

	/**
	 * Get many notes as small notes
	 *
	 * @param where Query to find notes
	 * @param take Number of notes to take
	 * @param offset Offset for query
	 *
	 * @return Notes, if exist
	 * */
	@JvmStatic
	fun getManySmall(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<SmallNote> = transaction {
		val notes = (NoteTable innerJoin UserTable)
			.join(replyingToAlias, JoinType.LEFT, NoteTable.replyingTo, replyingToAlias[NoteTable.id])
			.join(repeatAlias, JoinType.LEFT, NoteTable.repeat, repeatAlias[NoteTable.id])
			.selectAll()
			.where { where }
			.offset(offset)
			.let { NoteEntity.wrapRows(it) }
			.sortedByDescending { it.createdAt }
			.take(take)
			.toList()

		if (!notes.isEmpty())
			SmallNote.fromEntities(notes)
		else listOf()
	}

	/**
	 * Count notes
	 *
	 * @param where Query to find notes
	 *
	 * @return Count of notes
	 * */
	@JvmStatic
	fun count(where: Op<Boolean>): Long = transaction {
		NoteTable
			.leftJoin(UserTable)
			.select(where)
			.count()
	}

	/**
	 * Create a note
	 *
	 * @param id ID of the note
	 * @param user User authoring the post
	 * @param cw Content warning of the note
	 * @param content Content of the note
	 * @param visibility Visibility of the note
	 *
	 * @return Created note
	 * */
	@JvmStatic
	fun create(
		id: String = IdentifierService.generate(),
		user: UserEntity,
		cw: String?,
		content: String,
		visibility: Visibility,
	): Note {
		transaction {
			NoteEntity.new(id) {
				apId = ApIdService.renderNoteApId(id)
				this.user = user
				this.cw = if (cw != null) SanitizerService.sanitize(cw, true) else null
				this.content = SanitizerService.sanitize(content, true)
				this.visibility = visibility
				// todo: determine to, tags
			}
		}

		val note = getById(id)!!
		NoteCreateEvent(note).call()

		return note
	}

	/**
	 * Update an existing note
	 *
	 * @param id ID of the note
	 * @param user User authoring the post
	 * @param cw Content warning of the note
	 * @param content Content of the note
	 *
	 * @return Updated note
	 * */
	@JvmStatic
	fun update(
		id: String,
		user: UserEntity,
		cw: String?,
		content: String
	): Nothing = TODO()

	/**
	 * Like a note as a user, or removes a like if it's already there
	 *
	 * @param user User liking the note
	 * @param noteId ID of the target note
	 *
	 * @since 2025.9.1.1-SNAPSHOT
	 * */
	@JvmStatic
	fun like(
		user: User,
		noteId: String,
		toggle: Boolean = true,
	) {
		val note = getById(noteId)
			?: throw IllegalArgumentException("Note not found")

		if (!VisibilityService.canISee(note.visibility, note.user.id, note.to, user.id))
			throw IllegalArgumentException("Note not found")

		val existing = transaction {
			NoteLikeEntity
				.find {
					NoteLikeTable.note eq note.id and
							(NoteLikeTable.user eq user.id)
				}
				.singleOrNull()
		}

		if (existing != null && toggle) {
			transaction { existing.delete() }

			if (note.user.host == null)
				NotificationService.delete(
					NotificationTable.type eq NotificationType.Like and
							(NotificationTable.note eq note.id),
				)

			if (user.host == null) {
				val likeApId = ApIdService.renderActivityApId(existing.id.toString())

				ApDeliverService.deliverToFollowers<ApUndoActivity>(
					ApUndoActivity(
						"$likeApId/undo",
						`object` = ApIdOrObject.createObject {
							ApLikeActivity(
								likeApId,
								actor = user.apId,
								`object` = ApIdOrObject.Id(note.apId),
							)
						}
					),
					transaction { UserEntity[user.id] },
					if (note.user.host != null) listOf(note.user.inbox) else listOf()
				)
			}

			NoteUnlikeEvent(note, user).call()
			return
		}

		if (existing != null) return

		val likeId = IdentifierService.generate()

		transaction {
			val userEntity = UserEntity[user.id]
			val noteEntity = NoteEntity[note.id]

			NoteLikeEntity.new(likeId) {
				this.user = userEntity
				this.note = noteEntity
			}

			if (note.user.host == null && note.user.id != user.id)
				NotificationService.create(
					NotificationType.Like,
					noteEntity.user,
					userEntity,
					note
				)
		}

		if (user.host == null) {
			ApDeliverService.deliverToFollowers<ApLikeActivity>(
				ApLikeActivity(
					ApIdService.renderActivityApId(likeId),
					actor = user.apId,
					`object` = ApIdOrObject.Id(note.apId),
				),
				transaction { UserEntity[user.id] },
				if (note.user.host != null) listOf(note.user.inbox) else listOf()
			)
		}

		NoteLikeEvent(note, user).call()
	}

	/**
	 * Repeat or quote a note.
	 *
	 * @param user User repeating the note
	 * @param noteId ID of the target note
	 * @param content Quote content
	 *
	 * @return Created repeat or quote
	 */
	@JvmStatic
	fun repeat(
		user: User,
		noteId: String,
		cw: String? = null,
		content: String? = null,
		visibility: Visibility = Visibility.Direct
	): Note {
		val note = getById(noteId)
			?: throw IllegalArgumentException("Note not found")

		if (!VisibilityService.canISee(note.visibility, note.user.id, note.to, user.id))
			throw IllegalArgumentException("Note not found")

		val id = IdentifierService.generate()

		transaction {
			NoteEntity.new(id) {
				this.apId = ApIdService.renderNoteApId(id)
				this.user = UserEntity[user.id]
				this.cw = sanitizeOrNull { cw }
				this.content = sanitizeOrNull { content }
				this.visibility = visibility
				this.repeat = NoteEntity[note.id]
				this.to = listOf(note.user.id) // Always allow an author to see repeats of their posts
			}
		}

		val repeat = getById(id)
			?: throw InsertFailureException("Note not found")

		NoteRepeatEvent(repeat, note, user).call()

		val (to, cc) = ApVisibilityService.visibilityToCc(repeat.visibility, null, null)

		if (user.host == null) {
			ApDeliverService.deliverToFollowers<ApAnnounceActivity>(
				ApAnnounceActivity(
					repeat.id + "/activity",
					actor = user.apId,
					published = repeat.createdAt, // todo: format?
					`object` = ApIdOrObject.Id(note.apId),
					to = to,
					cc = cc
				),
				transaction { UserEntity[user.id] },
			)
		}

		return repeat
	}

	/**
	 * Delete a note
	 *
	 * @param where Query to find note
	 * */
	@JvmStatic
	fun delete(where: Op<Boolean>) = transaction {
		val entity = NoteEntity
			.find { where }
			.singleOrNull()
		if (entity == null) return@transaction

		NoteDeleteEvent(Note.fromEntity(entity)).call()
		entity.delete()
	}

	/**
	 * Prevent a note from sending notifications to the author
	 * or other mentioned local users.
	 *
	 * @param user User to mute notifications for
	 * @param noteId ID of note to mute
	 */
	@JvmStatic
	// TODO: duration?
	fun mute(
		user: User,
		noteId: String
	): Nothing = TODO()

	/**
	 * Delete a note by ID
	 *
	 * @param id ID of note
	 * */
	@JvmStatic
	fun deleteById(id: String) = delete(NoteTable.id eq id)

	/**
	 * Delete a note by ActivityPub ID
	 *
	 * @param apId ActivityPub ID of note
	 * */
	@JvmStatic
	fun deleteByApId(apId: String) = delete(NoteTable.id eq apId)
}
