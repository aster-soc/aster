package site.remlit.aster.service.ap

import io.ktor.http.Url
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import site.remlit.aster.common.util.orNull
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.model.ap.ApTypedObject
import site.remlit.aster.model.ap.ApValidationException
import site.remlit.aster.model.ap.ApValidationExceptionType
import site.remlit.aster.service.KeypairService
import site.remlit.aster.service.PolicyService
import site.remlit.aster.util.jsonConfig
import java.security.PublicKey
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Service for validating ActivityPub activities.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object ApValidationService : Service {
	private val logger: Logger = LoggerFactory.getLogger(ApValidationService::class.java)

	/**
	 * Validates an ActivityPub request
	 *
	 * @param request Routing request
	 * @param body Request body
	 * @param string Request body as string
	 *
	 * @return Requesting user
	 * */
	@JvmStatic
	@Suppress("LongMethod", "CyclomaticComplexMethod")
	suspend fun validate(
		request: RoutingRequest,
		body: ByteArray? = null,
		string: String? = null
	): UserEntity? {
		if (
			request.headers["Host"].isNullOrEmpty() ||
			request.headers["Host"] != Configuration.url.host
		) {
			throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Missing or invalid host"
			)
		}

		if (
			request.headers["Date"].isNullOrEmpty()
		) {
			throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Date not present"
			)
		}

		if (
			request.headers["Digest"].isNullOrEmpty()
		) {
			throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Digest not present"
			)
		}

		if (
			request.headers["Signature"].isNullOrEmpty()
		) {
			throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Signature not present"
			)
		}

		if (
			!request.headers["Digest"]!!.startsWith("SHA-256=")
		) {
			throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Digest uses unsupported algorithm"
			)
		}

		if (
			body != null &&
			isDigestValid(request.headers["Digest"]!!, body)
		) {
			throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Digest invalid"
			)
		}

		val signatureHeader = request.headers["Signature"]!!

		val keyIdRegex = buildHeaderRegex("keyId")
		val headersRegex = buildHeaderRegex("headers")
		val signatureRegex = buildHeaderRegex("signature")

		val sigKeyId = keyIdRegex.find(signatureHeader)?.groups?.get(1)?.value
			?: throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Could not extract keyId from Signature header"
			)
		val sigHeaders = headersRegex.find(signatureHeader)?.groups?.get(1)?.value?.split(" ")
			?: throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Could not extract headers from Signature header"
			)
		val sigSignature = signatureRegex.find(signatureHeader)?.groups?.get(1)?.value
			?: throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Could not extract signature from Signature header"
			)

		val signingString = ApSignatureService.createSigningString(
			request.httpMethod,
			request.path(),
			sigHeaders,
			request.headers.toMap()
		)

		val actorApId = sigKeyId.substringBefore("#")
		val actorHost = Url(actorApId).host

		val typedObj = if (string != null)
			orNull { jsonConfig.decodeFromString<ApTypedObject>(string) }
		else null

		if (PolicyService.shouldBlock(actorHost, typedObj?.type)) {
			throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Request blocked by local policy"
			)
		}

		val actor = ApActorService.resolve(actorApId)
		if (actor == null) {
			if (typedObj?.type == "Delete") {
				throw ApValidationException(
					ApValidationExceptionType.Ignore,
					"Actor not found and activity is Delete, ignoring"
				)
			}

			throw ApValidationException(
				ApValidationExceptionType.Unauthorized,
				"Actor not found"
			)
		}

		val isSignatureValid = try {
			isSignatureValid(
				sigSignature,
				signingString,
				KeypairService.pemToPublicKey(actor.publicKey),
				parseHttpDate(request.headers["Date"]!!)
			)
		} catch (e: Exception) {
			if (Configuration.debug) e.printStackTrace()
			throw ApValidationException(
				ApValidationExceptionType.Forbidden,
				"Signature invalid: ${e.message}"
			)
		}

		if (
			!isSignatureValid
		) {
			throw ApValidationException(
				ApValidationExceptionType.Forbidden,
				"Signature invalid"
			)
		}

		return actor
	}

	@JvmStatic
	fun isDigestValid(digest: String, data: ByteArray): Boolean {
		return digest == ApSignatureService.createDigest(data)
	}

	@JvmStatic
	@OptIn(ExperimentalTime::class)
	fun isSignatureValid(
		signature: String,
		signingString: String,
		publicKey: PublicKey,
		date: LocalDateTime
	): Boolean {
		// 150s is 2.5m, total of 5m window.
		// Iceshrimp.NET also does 5m.
		val maxTimeMargin = 150

		val nowPlusMargin = Clock.System.now().plus(maxTimeMargin.seconds)
		val nowMinusMargin = Clock.System.now().minus(maxTimeMargin.seconds)

		val dateInstant = date.toInstant(TimeZone.currentSystemDefault())

		if (dateInstant > nowPlusMargin)
			throw ApValidationException(
				ApValidationExceptionType.Forbidden,
				"Date is more than $maxTimeMargin seconds past now"
			)

		if (dateInstant < nowMinusMargin)
			throw ApValidationException(
				ApValidationExceptionType.Forbidden,
				"Date is more than $maxTimeMargin seconds from now"
			)

		val javaSignature = java.security.Signature.getInstance("SHA256withRSA")
		javaSignature.initVerify(publicKey)
		javaSignature.update(signingString.toByteArray())

		return javaSignature.verify(Base64.getDecoder().decode(signature))
	}

	@JvmStatic
	fun parseHttpDate(date: String): LocalDateTime {
		return ZonedDateTime
			.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME)
			.withZoneSameInstant(ZoneId.systemDefault())
			.toLocalDateTime()
			.toKotlinLocalDateTime()
	}

	/**
	 * Creates a regex pattern to get the value of a `key="value"` pattern.
	 * */
	@JvmStatic
	fun buildHeaderRegex(key: String) = Regex("$key=\"(.*?)\"")
}


