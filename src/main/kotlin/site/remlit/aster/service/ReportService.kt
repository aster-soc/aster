package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.Report
import site.remlit.aster.db.entity.NoteEntity
import site.remlit.aster.db.entity.ReportEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.ReportTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.exception.InsertFailureException
import site.remlit.aster.model.Configuration
import site.remlit.aster.util.model.fromEntities
import site.remlit.aster.util.model.fromEntity

/**
 * Service for managing reports
 *
 * 2025.11.4.0-SNAPSHOT
 * */
object ReportService {
	/**
	 * Reference the "sender" user on a report.
	 * For usage in queries.
	 * */
	@JvmStatic
	val senderAlias = UserTable.alias("sender")

	/**
	 * Reference the reported user on a report.
	 * For usage in queries.
	 * */
	@JvmStatic
	val userAlias = UserTable.alias("user")

	/**
	 * Get a report
	 *
	 * @param where Query to find report
	 *
	 * @return Report
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): Report? = transaction {
		val entity = ReportEntity
			.find { where }
			.singleOrNull()

		if (entity != null)
			Report.fromEntity(entity)
		else null
	}

	/**
	 * Get a report by its ID
	 *
	 * @param id ID of report
	 *
	 * @return Report
	 * */
	@JvmStatic
	fun getById(id: String): Report? = get(ReportTable.id eq id)

	/**
	 * Get reports
	 *
	 * @param where Query to find reports
	 * @param take Number of reports to take
	 * @param offset Offset for query
	 *
	 * @return Reports
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<Report> = transaction {
		val entities = ReportTable
			.join(senderAlias, JoinType.INNER, ReportTable.sender, senderAlias[UserTable.id])
			.join(userAlias, JoinType.LEFT, ReportTable.user, userAlias[UserTable.id])
			.selectAll()
			.where { where }
			.offset(offset)
			.let { ReportEntity.wrapRows(it) }
			.sortedByDescending { it.createdAt }
			.take(take)
			.toList()

		if (!entities.isEmpty())
			Report.fromEntities(entities)
		else listOf()
	}

	/**
	 * Create a report
	 *
	 * @param sender Sender of the report
	 * @param comment Comment left by sender
	 * @param note ID of note being reported
	 * @param user ID of user being reported
	 *
	 * @return Created report
	 * */
	@JvmStatic
	fun create(
		sender: UserEntity,
		comment: String? = null,
		note: String? = null,
		user: String? = null,
	): Report {
		if (note == null && user == null)
			throw IllegalArgumentException("A user or a note must be specified")

		val id = IdentifierService.generate()

		ReportEntity.new(id) {
			this.sender = sender
			this.comment = comment
			this.note = if (note != null) NoteEntity[note] else null
			this.user = if (user != null) UserEntity[user] else null
		}

		return getById(id)
			?: throw InsertFailureException("Failed to create report")
	}
}
