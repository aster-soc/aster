package site.remlit.aster.event.note

import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.User

/**
 * Event for when a note is bookmarked by a user
 *
 * @param note Note bookmarked
 * @param user User who bookmarked the note
 *
 * @since 2025.11.4.0-SNAPSHOT
 * */
class NoteBookmarkEvent(note: Note, user: User) : NoteInteractionEvent(note, user)
