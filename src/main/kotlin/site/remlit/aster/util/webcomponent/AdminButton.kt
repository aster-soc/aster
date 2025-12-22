package site.remlit.aster.util.webcomponent

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.onClick

fun FlowContent.adminButton(
	to: String,
	selected: Boolean = false,
	nav: Boolean = false,
	content: () -> Unit,
) = adminButton(content, to = to, selected = selected, nav = nav)

fun FlowContent.adminButton(
	onClick: () -> String,
	content: () -> Unit,
) = adminButton(content, onClick = onClick())

fun FlowContent.adminButton(
	content: () -> Unit,
	to: String? = null,
	onClick: String? = null,
	selected: Boolean = false,
	nav: Boolean = false
) {
	val classes = mutableSetOf("button")

	if (selected) classes.add("primary")
	if (nav) classes.add("nav")

	if (to != null) {
		a {
			this.classes = classes
			href = to
			if (onClick != null) this.onClick = onClick
			content()
		}
	} else {
		button {
			this.classes = classes
			if (onClick != null) this.onClick = onClick
			content()
		}
	}
}
