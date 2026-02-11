package site.remlit.aster.service.ap

import io.ktor.http.*
import site.remlit.aster.model.Service
import site.remlit.aster.util.capitalize
import java.security.MessageDigest
import java.security.PrivateKey
import kotlin.io.encoding.Base64
import kotlin.time.ExperimentalTime

/**
 * Service for validating ActivityPub signatures.
 *
 * @since 2025.12.1.0-SNAPSHOT
 * */
object ApSignatureService : Service {
	@JvmStatic
	fun createSigningString(
		method: HttpMethod,
		target: String,
		signatureHeaders: List<String>,
		requestHeaders: Map<String, List<String>>
	): String {
		var output = ""
		val headers = signatureHeaders.filter {
			it != "(request-target)"
		}

		output += "(request-target): ${method.toString().lowercase()} $target\n"

		headers.forEach {
			val value = requestHeaders[it.capitalize()]
				?.first { w -> w.isNotBlank() }

			output += "$it: $value"

			if (headers.indexOf(it) != headers.lastIndex)
				output += "\n"
		}

		return output
	}

	/**
	 * Creates a signature.
	 *
	 * @return Pair, first being the Signature header and the second the digest if a body was specified.
	 * */
	@JvmStatic
	@OptIn(ExperimentalTime::class)
	fun createSignature(
		target: String,
		method: HttpMethod,
		privateKey: PrivateKey,
		keyId: String,
		headers: Map<String, List<String>>,
		body: ByteArray? = null,
	): Pair<String, String?> {
		val headerKeys = mutableListOf<String>()

		headers.keys.forEach {
			headerKeys.add(it.lowercase())
		}

		val signingString = createSigningString(
			method,
			target,
			headerKeys,
			headers
		)

		val javaSignature = java.security.Signature.getInstance("SHA256withRSA")
		javaSignature.initSign(privateKey)
		javaSignature.update(signingString.toByteArray())

		var digest: String? = null
		if (body != null) {
			val md = MessageDigest.getInstance("SHA256withRSA")
			md.update(body)

			digest = "SHA-256=${Base64.encode(md.digest())}"
			javaSignature.update("\ndigest: $digest".toByteArray())
		}

		val signatureHeader = "keyId=\"$keyId\"," +
				"algorithm=\"rsa-sha256\"," +
				"headers=\"(request-target) ${headerKeys.joinToString(" ")}\"," +
				"signature=\"${Base64.encode(javaSignature.sign())}\""

		return Pair(signatureHeader, digest)
	}

	@JvmStatic
	fun createDigest(data: ByteArray): String =
		Base64.encode(MessageDigest.getInstance("SHA-256").digest(data))
}
