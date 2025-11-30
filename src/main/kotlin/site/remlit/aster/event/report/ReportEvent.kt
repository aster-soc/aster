package site.remlit.aster.event.report

import site.remlit.aster.common.model.Report
import site.remlit.aster.model.Event

/**
 * Event relating to reports
 *
 * @since 2025.11.4.0-SNAPSHOT
 * */
open class ReportEvent(val report: Report) : Event
