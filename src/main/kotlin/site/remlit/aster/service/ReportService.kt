package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.Op
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.User

/**
 * Service for managing reports
 *
 * 2025.11.4.0-SNAPSHOT
 * */
object ReportService {
	/**
	 * Get a report
	 *
	 * @param where Query to find report
	 *
	 * @return Report
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): Nothing = TODO()

	/**
	 * Get reports
	 *
	 * @param where Query to find reports
	 *
	 * @return Reports
	 * */
	@JvmStatic
	fun getMany(where: Op<Boolean>): Nothing = TODO()

	/**
	 * Create a report
	 *
	 * @param sender
	 * @param comment
	 * @param note
	 * @param user
	 *
	 * @return Created report
	 * */
	@JvmStatic
	fun create(
		sender: User,
		comment: String? = null,
		note: Note? = null,
		user: User? = null,
	): Nothing = TODO()
}
