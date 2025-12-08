package site.remlit.mfmkt

import site.remlit.mfmkt.model.MfmMention
import site.remlit.mfmkt.model.MfmText
import kotlin.test.Test
import kotlin.test.expect

class MfmKtTest {
	@Test
	fun parseTest() {
		val expectedResults = listOf(
			MfmText("biiig message blah blah "),
			MfmMention("test"),
			MfmText(" test test test "),
			MfmMention("test", "test.com"),
			MfmText(" test test")
		)

		val message = expectedResults.joinToString("")

		expect(expectedResults) { MfmKt.parse(message) }
	}
}
