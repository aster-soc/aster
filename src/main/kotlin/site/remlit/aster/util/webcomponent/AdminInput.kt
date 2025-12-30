package site.remlit.aster.util.webcomponent

import kotlinx.html.B
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.input
import kotlinx.html.onSubmit

fun FlowContent.adminInput(
	type: InputType,
	name: String = "",
	value: String = "",
	placeholder: String = "",
	onSubmit: String = "() => {}"
) {
	input {
		classes = setOf("input", "wide")

		this.type = type
		this.name = name
		this.value = value
		this.placeholder = placeholder
		this.onSubmit = onSubmit
	}
}
