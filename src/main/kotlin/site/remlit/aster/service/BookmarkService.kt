package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.NoteBookmarkEntity
import site.remlit.aster.db.entity.NoteEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.NoteBookmarkTable
import site.remlit.aster.event.note.NoteBookmarkEvent
import site.remlit.aster.event.note.NoteUnbookmarkEvent
import site.remlit.aster.model.Configuration
import site.remlit.aster.util.model.fromEntities

/**
 * Service for managing bookmarks
 *
 * @since 2025.11.4.0-SNAPSHOT
 * */
object BookmarkService {
	// No get, but may add later on. It seems really useless here.

	/**
	 * Get notes for found bookmarks
	 *
	 * @param where Query to find bookmarks
	 * @param take Number to take
	 * @param offset Number to offset
	 *
	 * @return List of bookmarked notes
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<Note> = transaction {
		val bookmarks =
			NoteBookmarkEntity
				.find { where }
				.offset(offset)
				.sortedByDescending { it.createdAt }
				.take(take)
				.toList()

		if (!bookmarks.isEmpty())
			Note.fromEntities(bookmarks.map { it.note })
		else listOf()
	}

	/**
	 * Bookmark a note as a user, or removes a bookmark if it's already there
	 *
	 * @param user User bookmarking the note
	 * @param note ID of the target note
	 * */
	@JvmStatic
	fun create(
		user: User,
		note: String
	) {
		val note = NoteService.getById(note)
			?: throw IllegalArgumentException("Note not found")

		if (!VisibilityService.canISee(note.visibility, note.user.id, note.to, user.id))
			throw IllegalArgumentException("Note not found")

		val existing = transaction {
			NoteBookmarkEntity
				.find {
					NoteBookmarkTable.note eq note.id and
							(NoteBookmarkTable.user eq user.id)
				}
				.singleOrNull()
		}

		if (existing != null) {
			transaction { existing.delete() }

			NoteUnbookmarkEvent(note, user).call()
			return
		}


		transaction {
			val userEntity = UserEntity[user.id]
			val noteEntity = NoteEntity[note.id]

			NoteBookmarkEntity.new(IdentifierService.generate()) {
				this.user = userEntity
				this.note = noteEntity
			}
		}

		NoteBookmarkEvent(note, user).call()
	}
}
