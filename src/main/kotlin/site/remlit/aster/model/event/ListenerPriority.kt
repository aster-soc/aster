package site.remlit.aster.model.event

enum class ListenerPriority {
	/** Highest priority, will be executed first */
	High,

	/** Regular priority */
	Normal,

	/** Lowest priority, will be executed last */
	Low
}
