package site.remlit.aster.event.notification

import site.remlit.aster.common.model.Notification
import site.remlit.aster.model.event.Event

/**
 * Event related to a notification
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
open class NotificationEvent(val notification: Notification) : Event
