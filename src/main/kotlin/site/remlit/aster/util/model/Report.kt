package site.remlit.aster.util.model

import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.Report
import site.remlit.aster.common.model.User
import site.remlit.aster.db.entity.ReportEntity

fun Report.Companion.fromEntity(entity: ReportEntity): Report = Report(
	id = entity.id.toString(),
	comment = entity.comment,
	user = if (entity.user != null) User.fromEntity(entity.user!!) else null,
	note = if (entity.note != null) Note.fromEntity(entity.note!!) else null,
)

fun Report.Companion.fromEntities(entities: List<ReportEntity>): List<Report> =
	entities.map { fromEntity(it) }
