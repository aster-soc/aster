package site.remlit.aster.common.util

fun List<Int>.median(): Int {
	val n = this.sorted()
	return if (n.size % 2 == 0)
		(n[n.size / 2] + n[(n.size - 1) / 2]) / 2
	else
		n[n.size / 2]
}

fun List<Long>.median(): Long {
	val n = this.sorted()
	return if (n.size % 2 == 0)
		(n[n.size / 2] + n[(n.size - 1) / 2]) / 2
	else
		n[n.size / 2]
}

fun List<Double>.median(): Double {
	val n = this.sorted()
	return if (n.size % 2 == 0)
		(n[n.size / 2] + n[(n.size - 1) / 2]) / 2
	else
		n[n.size / 2]
}
