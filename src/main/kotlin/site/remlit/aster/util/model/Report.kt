package site.remlit.aster.util.model

import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.Report
import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.ReportEntity
import site.remlit.aster.util.toLocalInstant

fun Report.Companion.fromEntity(entity: ReportEntity): Report = Report(
	id = entity.id.toString(),
	sender = User.fromEntity(entity.sender),
	comment = entity.comment,
	user = if (entity.user != null) User.fromEntity(entity.user!!) else null,
	note = if (entity.note != null) Note.fromEntity(entity.note!!) else null,
	resolvedBy = if (entity.resolvedBy != null) User.fromEntity(entity.resolvedBy!!) else null,
	createdAt = entity.createdAt.toLocalInstant(),
	updatedAt = entity.updatedAt?.toLocalInstant(),
)

fun Report.Companion.fromEntities(entities: List<ReportEntity>): List<Report> =
	entities.map { fromEntity(it) }
