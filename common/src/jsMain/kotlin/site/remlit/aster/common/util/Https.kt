package site.remlit.aster.common.util

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise

@JsExport
object Https {
	private fun start() {}

	private fun end(res: Promise<Response>): Promise<JsAny> =
		MainScope().promise {
			val res = res.await()
			val body: dynamic = orNull { res.json().await() }

			if (!res.ok)
				throw JsException("${res.status} ${body?.message ?: "Something went wrong"}")

			console.log(body)

			return@promise body
		}

	private val token: String? get() = window.localStorage.getItem("aster_token")

	private fun createHeaders(vararg additional: Pair<String, String>): dynamic {
		val headers: dynamic = object {}
		headers["Authorization"] = "Bearer $token"
		additional.forEach { (k, v) -> headers[k] = v }
		return headers
	}

	fun get(url: String, auth: Boolean = false): JsAny {
		start()

		val request = window.fetch(
			url, RequestInit(
				method = "GET",
				headers = if (auth) createHeaders() else null,
			)
		)

		return end(request)
	}

	fun post(url: String, auth: Boolean = false, body: JsAny): JsAny {
		start()

		val request = window.fetch(
			url, RequestInit(
				method = "POST",
				headers = if (auth) createHeaders(Pair("Content-Type", "application/json")) else null,
				body = JSON.stringify(body)
			)
		)

		return end(request)
	}

	fun delete(url: String): JsAny {
		start()

		val request = window.fetch(
			url, RequestInit(
				method = "DELETE",
				headers = createHeaders(),
			)
		)

		return end(request)
	}
}
