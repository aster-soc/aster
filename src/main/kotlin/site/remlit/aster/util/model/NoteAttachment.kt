package site.remlit.aster.util.model

import site.remlit.aster.common.model.DriveFile
import site.remlit.aster.common.model.NoteAttachment

fun NoteAttachment.Companion.fromDriveFile(entity: DriveFile): NoteAttachment {
	return NoteAttachment(
		src = entity.src,
		alt = entity.alt,
		type = entity.type,
	)
}

fun NoteAttachment.Companion.fromDriveFiles(entities: List<DriveFile>): List<NoteAttachment> =
	entities.map { fromDriveFile(it) }
