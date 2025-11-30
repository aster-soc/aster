package site.remlit.aster.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.http.*
import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory
import site.remlit.aster.common.model.type.PolicyType
import site.remlit.aster.exception.ResolverException
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.PackageInformation
import site.remlit.aster.model.Service
import site.remlit.aster.service.ap.ApSignatureService
import site.remlit.aster.util.jsonConfig
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.ExperimentalTime

/**
 * Service for resolving URLs and ActivityPub objects.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object ResolverService : Service {
	private val logger = LoggerFactory.getLogger(ResolverService::class.java)

	/**
	 * Range from 200 to 299, acceptable response status
	 * codes indicating no error.
	 * */
	@JvmStatic
	@Suppress("MagicNumber")
	val okRange = 200 .. 299

	/**
	 * Creates an HTTP client with default request headers
	 * and content negotiation rules for ActivityPub and more.
	 *
	 * @return Created HTTP client
	 * */
	@JvmStatic
	fun createClient(): HttpClient {
		return HttpClient(CIO) {
			defaultRequest {
				headers.append(
					"User-Agent",
					"${PackageInformation.name}/${PackageInformation.version} (+${Configuration.url})"
				)
			}

			install(ContentNegotiation) {
				json(jsonConfig)

				register(
					ContentType.parse("application/ld+json"),
					KotlinxSerializationConverter(jsonConfig)
				)
				register(
					ContentType.parse("application/activity+json"),
					KotlinxSerializationConverter(jsonConfig)
				)
				register(
					ContentType.parse("application/jrd+json"),
					KotlinxSerializationConverter(jsonConfig)
				)
			}
		}
	}

	/**
	 * Resolve a URL
	 *
	 * In most cases, it's reccomended to use resolveSigned instead
	 *
	 * @param url URL to resolve as a String
	 * @param accept Accept header's content, defaults to application/activity+json
	 *
	 * @return JsonObject or null
	 * */
	@JvmStatic
	suspend fun resolve(url: String, accept: String = "application/activity+json"): JsonObject? {
		val blockPolicies = PolicyService.getAllByType(PolicyType.Block)
		val blockedHosts = PolicyService.reducePoliciesToHost(blockPolicies)

		if (blockedHosts.contains(Url(url).host))
			return null

		try {
			val client = createClient()
			val response = client.get(url) {
				headers.append("Accept", accept)
			}
			client.close()

			logger.info("${response.status} ${response.request.method} - ${response.request.url}")

			if (response.status.value !in okRange)
				throw ResolverException(response.status, response.status.description)

			val body: JsonObject? = response.body()
			return body
		} catch (e: Exception) {
			logger.info("Request failed: ${e.message}")
			return null
		}
	}

	/**
	 * Resolve a URL, signed
	 *
	 * @param url URL to resolve as a String
	 * @param accept Accept header's content, defaults to application/activity+json
	 * @param user User to sign the request as
	 *
	 * @return JsonObject or null
	 * */
	@JvmStatic
	@OptIn(ExperimentalTime::class)
	suspend fun resolveSigned(
		url: String,
		accept: String = "application/activity+json",
		user: String? = null
	): JsonObject? {
		val url = Url(url)

		val date = LocalDateTime.now(ZoneId.of("GMT"))
			.toHttpDateString()

		val blockPolicies = PolicyService.getAllByType(PolicyType.Block)
		val blockedHosts = PolicyService.reducePoliciesToHost(blockPolicies)

		if (blockedHosts.contains(url.host))
			return null

		val actor = (if (user != null) UserService.getByUsername(user) else null)
			?: UserService.getInstanceActor()

		val actorPrivate = UserService.getPrivateById(actor.id.toString())!!

		try {
			val client = createClient()
			val response = client.get(url) {
				headers.append("Host", url.host)
				headers.append("Date", date)
				headers.append("Accept", accept)

				val sig = ApSignatureService.createSignature(
					url.encodedPath,
					HttpMethod.Get,
					KeypairService.pemToPrivateKey(actorPrivate.privateKey),
					actor.apId + "#main-key",
					mapOf(
						"Host" to listOf(url.host),
						"Date" to listOf(date)
					)
				)

				headers.append("Signature", sig.first)
			}
			client.close()

			if (response.status.value !in okRange)
				throw ResolverException(response.status, response.status.description)
			else logger.info("${response.status} ${response.request.method} - ${response.request.url}")

			val body: JsonObject? = response.body()
			return body
		} catch (e: Exception) {
			logger.info("Request failed: ${e.message}")
			return null
		}
	}
}
