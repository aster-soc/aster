package site.remlit.aster.util.webcomponent

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.header
import kotlinx.html.span
import site.remlit.aster.common.model.type.InstanceRegistrationsType
import site.remlit.aster.model.Configuration

fun FlowContent.adminHeader(currentPage: String) {
	fun FlowContent.adminHeaderButton(page: String, to: String) {
		a {
			classes = if (currentPage == page) setOf("selected") else setOf()
			href = to
			+page
		}
	}

	header {
		div {
			classes = setOf("inner")
			span { +"Admin Panel" }
			div {
				classes = setOf("buttons")
				adminHeaderButton("Overview", "/admin")
				adminHeaderButton("Users", "/admin/users")
				adminHeaderButton("Instances", "/admin/instances")
				if (Configuration.registrations == InstanceRegistrationsType.Invite)
					adminHeaderButton("Invites", "/admin/invites")
				adminHeaderButton("Queues", "/admin/queues")
				adminHeaderButton("Reports", "/admin/reports")
				adminHeaderButton("Plugins", "/admin/plugins")
				adminHeaderButton("Debug", "/admin/debug")
			}
		}
	}
}
