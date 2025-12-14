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

						adminButton("/admin", path == "/admin") { +"Overview" }
						adminButton("/admin/users", path == "/admin/users") { +"Users" }
						adminButton("/admin/instances", path == "/admin/instances") { +"Instances" }

						if (Configuration.registrations == InstanceRegistrationsType.Invite)
							adminButton("/admin/invites", path == "/admin/invites") { +"Invites" }

						adminButton("/admin/queues", path == "/admin/queues") { +"Queues" }
						adminButton("/admin/reports", path == "/admin/reports") { +"Reports" }
						adminButton("/admin/plugins", path == "/admin/plugins") { +"Plugins" }

						if (Configuration.debug)
							adminButton("/admin/debug", path == "/admin/debug") { +"Debug" }
					}
				}
				div("pageWrapper full") {
					content()
				}
			}
		}
	}
}
