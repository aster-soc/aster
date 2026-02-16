package site.remlit.aster.service

import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import site.remlit.aster.model.Service

/**
 * Service for sanitizing and escaping user submitted content.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object SanitizerService : Service {
	private val allowedElements = arrayOf("a", "p", "span")

	val policy: PolicyFactory = HtmlPolicyBuilder()
		.allowElements(*allowedElements)
		.allowAttributes("href").onElements("a")
		.allowStandardUrlProtocols()
		.requireRelNofollowOnLinks()
		.toFactory()

	/**
	 * Sanitizes user input
	 *
	 * @param string String to sanitize
	 *
	 * @return Sanitized string
	 * */
	@JvmStatic
	fun sanitize(string: String): String =
		policy.sanitize(string)
			.replace("&#64;", "@")
}
