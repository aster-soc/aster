package site.remlit.aster.util.webcomponent

import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.ctn(content: FlowContent.() -> Unit) {
	div("ctn") {
		content()
	}
}
