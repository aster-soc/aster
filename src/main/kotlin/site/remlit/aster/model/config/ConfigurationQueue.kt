package site.remlit.aster.model.config

@Suppress("MagicNumber")
class ConfigurationQueue : ConfigurationObject {
	val inbox: ConfigurationSpecificQueue
		get() = ConfigurationSpecificQueue(
			(config?.propertyOrNull("queue.inbox.concurrency")?.getString()?.toInt() ?: 5),
			((config?.propertyOrNull("queue.deliver.maxRetries")?.getString()?.toInt()) ?: 10)
		)
	val deliver: ConfigurationSpecificQueue
		get() = ConfigurationSpecificQueue(
			((config?.propertyOrNull("queue.deliver.concurrency")?.getString()?.toInt()) ?: 20),
			((config?.propertyOrNull("queue.deliver.maxRetries")?.getString()?.toInt()) ?: 15)
		)
	val backfill: ConfigurationSpecificQueue
		get() = ConfigurationSpecificQueue(
			((config?.propertyOrNull("queue.backfill.concurrency")?.getString()?.toInt()) ?: 4),
			((config?.propertyOrNull("queue.backfill.maxRetries")?.getString()?.toInt()) ?: 10)
		)
}
