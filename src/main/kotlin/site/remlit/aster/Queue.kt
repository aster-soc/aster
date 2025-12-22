package site.remlit.aster

import io.ktor.server.application.*
import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.registry.InboxHandlerRegistry
import site.remlit.aster.service.QueueService

@ApiStatus.Internal
internal fun Application.configureQueue() {
	InboxHandlerRegistry.registerDefaults()
	QueueService.initialize()
}
