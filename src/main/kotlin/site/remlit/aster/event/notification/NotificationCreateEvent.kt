package site.remlit.aster.event.notification

import site.remlit.aster.common.model.Notification

/**
 * Event fired when a notification is created
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
class NotificationCreateEvent(notification: Notification) : NotificationEvent(notification)
