package site.remlit.aster.event.report

import site.remlit.aster.common.model.Report

/**
 * Event for when a report is resolved.
 * Indicates that action has been taken.
 *
 * @since 2025.11.4.0-SNAPSHOT
 * */
class ReportResolveEvent(report: Report) : ReportEvent(report)
