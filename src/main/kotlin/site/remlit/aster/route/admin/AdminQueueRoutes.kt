package site.remlit.aster.route.admin

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.classes
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.ul
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.type.RoleType
import site.remlit.aster.db.entity.BackfillQueueEntity
import site.remlit.aster.db.entity.DeliverQueueEntity
import site.remlit.aster.db.entity.InboxQueueEntity
import site.remlit.aster.db.table.BackfillQueueTable
import site.remlit.aster.db.table.DeliverQueueTable
import site.remlit.aster.db.table.InboxQueueTable
import site.remlit.aster.model.QueueStatus
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.QueueService
import site.remlit.aster.util.authentication
import site.remlit.aster.util.model.fromEntity
import site.remlit.aster.util.webcomponent.adminPage

internal object AdminQueueRoutes {
	fun register() =
		RouteRegistry.registerRoute {
			authentication(
				required = true,
				role = RoleType.Admin
			) {
				get("/admin/queues") {
					val inboxPending: MutableList<InboxQueueEntity> = mutableListOf()
					val inboxCompleted: MutableList<InboxQueueEntity> = mutableListOf()
					val inboxFailed: MutableList<InboxQueueEntity> = mutableListOf()

					val deliverPending: MutableList<DeliverQueueEntity> = mutableListOf()
					val deliverCompleted: MutableList<DeliverQueueEntity> = mutableListOf()
					val deliverFailed: MutableList<DeliverQueueEntity> = mutableListOf()

					val backfillPending: MutableList<BackfillQueueEntity> = mutableListOf()
					val backfillCompleted: MutableList<BackfillQueueEntity> = mutableListOf()
					val backfillFailed: MutableList<BackfillQueueEntity> = mutableListOf()

					transaction {
						InboxQueueEntity
							.find { InboxQueueTable.status eq QueueStatus.PENDING }
							.forEach { e ->
								inboxPending.add(e)
							}
						InboxQueueEntity
							.find { InboxQueueTable.status eq QueueStatus.COMPLETED }
							.forEach { e ->
								inboxCompleted.add(e)
							}
						InboxQueueEntity
							.find { InboxQueueTable.status eq QueueStatus.FAILED }
							.forEach { e ->
								inboxFailed.add(e)
							}

						DeliverQueueEntity
							.find { DeliverQueueTable.status eq QueueStatus.PENDING }
							.forEach { e ->
								deliverPending.add(e)
							}
						DeliverQueueEntity
							.find { DeliverQueueTable.status eq QueueStatus.COMPLETED }
							.forEach { e ->
								deliverCompleted.add(e)
							}
						DeliverQueueEntity
							.find { DeliverQueueTable.status eq QueueStatus.FAILED }
							.forEach { e ->
								deliverFailed.add(e)
							}

						BackfillQueueEntity
							.find { BackfillQueueTable.status eq QueueStatus.PENDING }
							.forEach { e ->
								backfillPending.add(e)
							}
						BackfillQueueEntity
							.find { BackfillQueueTable.status eq QueueStatus.COMPLETED }
							.forEach { e ->
								backfillCompleted.add(e)
							}
						BackfillQueueEntity
							.find { BackfillQueueTable.status eq QueueStatus.FAILED }
							.forEach { e ->
								backfillFailed.add(e)
							}
					}

					call.respondHtml(HttpStatusCode.OK) {
						adminPage(call.route.path) {
							h2 { +"Inbox" }
							transaction {
								div {
									this.classes = setOf("ctn")
									div {
										this.classes = setOf("ctn", "column")
										span {
											+"${inboxPending.size} jobs pending"
										}
										ul {
											for (job in inboxPending) {
												li {
													a {
														href = "/admin/queues/inbox/job/${job.id}"
														+"${job.id} ${job.sender?.host}"
													}
												}
											}
										}
									}
									div {
										this.classes = setOf("ctn", "column")
										span {
											+"${inboxCompleted.size} jobs completed"
										}
										ul {
											for (job in inboxCompleted) {
												li {
													a {
														href = "/admin/queues/inbox/job/${job.id}"
														+"${job.id} ${job.sender?.host}"
													}
												}
											}
										}
									}
									div {
										this.classes = setOf("ctn", "column")
										span {
											+"${inboxFailed.size} jobs failed"
										}
										ul {
											for (job in inboxFailed) {
												li {
													a {
														href = "/admin/queues/inbox/job/${job.id}"
														+"${job.id} ${job.sender?.host}"
													}
												}
											}
										}
									}
								}
								h2 { +"Deliver" }
								div {
									this.classes = setOf("ctn")
									div {
										this.classes = setOf("ctn", "column")
										span {
											+"${deliverPending.size} jobs pending"
										}
										ul {
											for (job in deliverPending) {
												li {
													a {
														href = "/admin/queues/deliver/job/${job.id}"
														+"${job.id} ${job.inbox}"
													}
												}
											}
										}
									}
									div {
										this.classes = setOf("ctn", "column")
										span {
											+"${deliverCompleted.size} jobs completed"
										}
										ul {
											for (job in deliverCompleted) {
												li {
													a {
														href = "/admin/queues/deliver/job/${job.id}"
														+"${job.id} ${job.inbox}"
													}
												}
											}
										}
									}
									div {
										this.classes = setOf("ctn", "column")
										span {
											+"${deliverFailed.size} jobs failed"
										}
										ul {
											for (job in deliverFailed) {
												li {
													a {
														href = "/admin/queues/deliver/job/${job.id}"
														+"${job.id} ${job.inbox}"
													}
												}
											}
										}
									}
								}
								h2 { +"Backfill" }
								div {
									this.classes = setOf("ctn")
									div {
										this.classes = setOf("ctn", "column")
										span {
											+"${backfillPending.size} jobs pending"
										}
										ul {
											for (job in backfillPending) {
												li {
													a {
														href = "/admin/queues/backfill/job/${job.id}"
														+"${job.id} ${job.target}"
													}
												}
											}
										}
									}
									div {
										this.classes = setOf("ctn", "column")
										span {
											+"${backfillCompleted.size} jobs completed"
										}
										ul {
											for (job in backfillCompleted) {
												li {
													a {
														href = "/admin/queues/backfill/job/${job.id}"
														+"${job.id} ${job.target}"
													}
												}
											}
										}
									}
									div {
										this.classes = setOf("ctn", "column")
										span {
											+"${backfillFailed.size} jobs failed"
										}
										ul {
											for (job in backfillFailed) {
												li {
													a {
														href = "/admin/queues/backfill/job/${job.id}"
														+"${job.id} ${job.target}"
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}

				get("/admin/queues/{queue}/job/{id}") {
					val queue = call.parameters.getOrFail("queue")
					val id = call.parameters.getOrFail("id")

					when (queue) {
						"inbox" -> {
							val job = QueueService.getInboxJob(InboxQueueTable.id eq id)
								?: throw IllegalArgumentException("Job not found")

							call.respondHtml {
								adminPage(call.route.path) {
									transaction {
										h1 { +id }

										if (job.sender != null) {
											b { +"Sender" }
											p { +"${User.fromEntity(job.sender!!)}" }
										}

										b { +"Status" }
										p { +"${job.status}" }

										b { +"Content" }
										p { code { +String(job.content.bytes) } }

										b { +"Created at" }
										p { +"${job.createdAt}" }

										b { +"Retry at" }
										p { +"${job.retryAt}" }

										b { +"Retries" }
										p { +"${job.retries}" }

										b { +"Stack trace" }
										p { code { +"${job.stacktrace}" } }
									}
								}
							}
						}

						"deliver" -> {
							val job = QueueService.getDeliverJob(DeliverQueueTable.id eq id)
								?: throw IllegalArgumentException("Job not found")

							call.respondHtml {
								adminPage(call.route.path) {
									transaction {
										h1 { +id }

										if (job.sender != null) {
											b { +"Sender" }
											p { +"${User.fromEntity(job.sender!!)}" }
										}

										b { +"Inbox" }
										p { +job.inbox }

										b { +"Status" }
										p { +"${job.status}" }

										b { +"Content" }
										p { code { +String(job.content.bytes) } }

										b { +"Created at" }
										p { +"${job.createdAt}" }

										b { +"Retry at" }
										p { +"${job.retryAt}" }

										b { +"Retries" }
										p { +"${job.retries}" }

										b { +"Stack trace" }
										p { code { +"${job.stacktrace}" } }
									}
								}
							}
						}

						"backfill" -> {
							val job = QueueService.getBackfillJob(BackfillQueueTable.id eq id)
								?: throw IllegalArgumentException("Job not found")

							call.respondHtml {
								adminPage(call.route.path) {
									transaction {
										h1 { +id }

										b { +"Target" }
										p { +job.target }

										b { +"Type" }
										p { code { +job.backfillType.toString() } }

										b { +"Status" }
										p { +"${job.status}" }

										b { +"Created at" }
										p { +"${job.createdAt}" }

										b { +"Retry at" }
										p { +"${job.retryAt}" }

										b { +"Retries" }
										p { +"${job.retries}" }

										b { +"Stack trace" }
										p { code { +"${job.stacktrace}" } }
									}
								}
							}
						}

						else -> throw IllegalArgumentException("Queue not found")
					}
				}
			}
		}
}
