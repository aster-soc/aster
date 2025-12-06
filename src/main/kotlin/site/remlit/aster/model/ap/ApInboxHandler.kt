package site.remlit.aster.model.ap

import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.db.entity.InboxQueueEntity

/**
 * Handler for inbox activities.
 * To be registered with the InboxHandlerRegistry.
 * */
// Consider making this <T>
@ApiStatus.OverrideOnly
interface ApInboxHandler {
	suspend fun handle(job: InboxQueueEntity)
}
