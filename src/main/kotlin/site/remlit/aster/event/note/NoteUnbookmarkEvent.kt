package site.remlit.aster.event.note

import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.User

/**
 * Event for when a note is unbookmarked by a user
 *
 * @param note Note unbookmarked
 * @param user User who unbookmarked the note
 *
 * @since 2025.11.4.0-SNAPSHOT
 * */
class NoteUnbookmarkEvent(note: Note, user: User) : NoteInteractionEvent(note, user)
