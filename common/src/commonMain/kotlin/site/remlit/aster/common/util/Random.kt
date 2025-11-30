package site.remlit.aster.common.util

import kotlin.js.JsExport
import kotlin.random.Random

/**
 * Generate a basic, safe, random string.
 * */
@JsExport
@Suppress("MagicNumber")
fun randomString(): String {
	val letters = listOf(
		"a",
		"b",
		"c",
		"d",
		"e",
		"f",
		"g",
		"h",
		"i",
		"j",
		"k",
		"l",
		"m",
		"n",
		"o",
		"p",
		"q",
		"r",
		"s",
		"t",
		"u",
		"v",
		"w",
		"x",
		"y",
		"z"
	)

	var generated = ""

	repeat(32) {
		val hit = Random.nextInt(2)
		when (hit) {
			1 -> generated += letters[Random.nextInt(letters.size)]
			else -> generated += Random.nextInt(9)
		}
	}

	return generated
}
