package site.remlit.aster.util.webcomponent

import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.id

fun FlowContent.adminListNav(base: Long, take: Int, query: String?) {
	div {
		classes = setOf("ctn")

		val linkBase = if (query == null)
			"?offset="
		else "?q=$query&offset="

		adminButton("$linkBase${base - take}") {
			ti("chevron-left")
			+"Backwards"
		}
		adminButton("$linkBase${base + take}") {
			ti("chevron-right")
			+"Forwards"
		}
		form {
			classes = setOf("wide")
			id = "admin-list-nav-search"
			adminInput(InputType.text, "query", query ?: "", "Search query")
		}
	}
}
