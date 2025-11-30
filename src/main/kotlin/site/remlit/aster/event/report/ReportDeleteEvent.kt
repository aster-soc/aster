package site.remlit.aster.event.report

import site.remlit.aster.common.model.Report

/**
 * Event for when a report is deleted
 *
 * @since 2025.11.4.0-SNAPSHOT
 * */
class ReportDeleteEvent(report: Report) : ReportEvent(report)
