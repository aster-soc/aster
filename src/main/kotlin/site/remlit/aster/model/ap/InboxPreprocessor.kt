package site.remlit.aster.model.ap

import site.remlit.aster.db.entity.InboxQueueEntity

/**
 * Preprocessor for inbox jobs. Can be used to modify job content before processed.
 * Must be registered with the InboxHandlerRegistry with the registerPreprocessor method.
 * */
interface InboxPreprocessor {
	suspend fun preprocess(job: InboxQueueEntity?): InboxQueueEntity?
}

