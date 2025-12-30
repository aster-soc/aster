package site.remlit.aster.util.webcomponent

import com.github.nwillc.ksvg.elements.SVG
import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.i
import kotlinx.html.svg
import kotlinx.html.unsafe

fun FlowContent.ti(name: String) {
	i {
		classes = setOf("ti", "ti-$name")
	}
}
