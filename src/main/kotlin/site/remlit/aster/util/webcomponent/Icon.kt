package site.remlit.aster.util.webcomponent

import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.i

fun FlowContent.ti(name: String) {
	i {
		classes = setOf("ti", "ti-$name")
	}
}
