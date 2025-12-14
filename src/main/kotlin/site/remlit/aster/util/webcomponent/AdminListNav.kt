package site.remlit.aster.util.webcomponent

import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div

fun FlowContent.adminListNav(base: Long, take: Int) {
	div {
		classes = setOf("ctn")
		adminButton("?offset=${base - take}") {
			+"Backwards"
		}
		adminButton("?offset=${base + take}") {
			+"Forwards"
		}
	}
}
