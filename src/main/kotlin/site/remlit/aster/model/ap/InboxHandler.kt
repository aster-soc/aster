package site.remlit.aster.model.ap

import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.db.entity.InboxQueueEntity

/**
 * Handler for inbox activities.
 * Must be registered with the InboxHandlerRegistry.
 * */
@ApiStatus.OverrideOnly
interface InboxHandler {
	suspend fun handle(job: InboxQueueEntity)
}
