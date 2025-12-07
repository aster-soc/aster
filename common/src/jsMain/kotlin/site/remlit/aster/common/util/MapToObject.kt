package site.remlit.aster.common.util

fun Map<*, *>.toObject(): dynamic {
	val obj: dynamic = object {}

	this.entries.forEach { entry ->
		obj[entry.key] = entry.value
	}

	return obj
}
