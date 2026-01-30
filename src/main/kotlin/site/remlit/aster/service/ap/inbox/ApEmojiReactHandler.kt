package site.remlit.aster.service.ap.inbox

import org.slf4j.LoggerFactory
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.model.ap.InboxHandler

class ApEmojiReactHandler : InboxHandler {
	private val logger = LoggerFactory.getLogger(ApEmojiReactHandler::class.java)

	override suspend fun handle(job: InboxQueueEntity) {
		throw NotImplementedError()
	}
}
