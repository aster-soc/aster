package site.remlit.aster.event.notification

import site.remlit.aster.common.model.Notification

/**
 * Event fired when a notification is deleted
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
class NotificationDeleteEvent(notification: Notification) : NotificationEvent(notification)
