package site.remlit.aster.model.ap

import site.remlit.aster.db.entity.DeliverQueueEntity

/**
 * Preprocessor for deliver jobs. Can be used to modify job content before processed.
 * Must be registered with the ApDeliverService with the registerPreprocessor method.
 * */
interface DeliverPreprocessor {
	suspend fun preprocess(job: DeliverQueueEntity?): DeliverQueueEntity?
}
