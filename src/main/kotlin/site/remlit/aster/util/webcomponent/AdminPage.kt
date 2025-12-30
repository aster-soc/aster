package site.remlit.aster.util.webcomponent

import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.main
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.styleLink
import kotlinx.html.title
import site.remlit.aster.common.model.type.InstanceRegistrationsType
import site.remlit.aster.model.Configuration

fun HTML.adminPage(path: String, content: FlowContent.() -> Unit) {
	head {
		title { +"Admin Panel" }
		styleLink("/admin/assets/index.css")
		styleLink("/admin/assets/tabler-icons.min.css")
		script { src = "/admin/assets/index.js" }
	}
	body {
		div {
			id = "root"

			main {
				div("pageHeader") {
					span { +"Admin Panel" }
					div {
						classes = setOf("ctn")

						adminButton("/admin", path == "/admin", true) { +"Overview" }
						adminButton("/admin/users", path == "/admin/users", true) { +"Users" }
						adminButton("/admin/instances", path == "/admin/instances", true) { +"Instances" }

						if (Configuration.registrations == InstanceRegistrationsType.Invite)
							adminButton("/admin/invites", path == "/admin/invites", true) { +"Invites" }

						adminButton("/admin/queues", path == "/admin/queues", true) { +"Queues" }
						adminButton("/admin/reports", path == "/admin/reports", true) { +"Reports" }
						adminButton("/admin/plugins", path == "/admin/plugins", true) { +"Plugins" }
						adminButton("/admin/policies", path == "/admin/policies", true) { +"Policies" }

						if (Configuration.debug)
							adminButton("/admin/debug", path == "/admin/debug", true) { +"Debug" }
					}
				}
				div("pageWrapper full") {
					content()
				}
			}
		}
	}
}
