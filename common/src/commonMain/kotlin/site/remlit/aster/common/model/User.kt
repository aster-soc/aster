package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant

@JsExport
@Serializable
data class User(
	val id: String,

	val apId: String,
	val inbox: String,
	val outbox: String?,

	val username: String,
	val host: String? = null,
	val displayName: String? = null,
	val bio: String? = null,
	val location: String? = null,
	val birthday: String? = null,

	val avatar: String? = null,
	val avatarAlt: String? = null,
	val avatarBlurHash: String? = null,

	val banner: String? = null,
	val bannerAlt: String? = null,
	val bannerBlurHash: String? = null,

	val locked: Boolean = false,
	val suspended: Boolean = false,
	val activated: Boolean = false,
	val automated: Boolean = false,
	val discoverable: Boolean = false,
	val indexable: Boolean = false,
	val sensitive: Boolean = false,

	val isCat: Boolean = false,
	val speakAsCat: Boolean = false,

	val followersUrl: String? = null,
	val followingUrl: String? = null,

	val createdAt: Instant,
	val updatedAt: Instant? = null,

	val publicKey: String
) {
	fun renderHandle(): String =
		site.remlit.aster.common.util.renderHandle(this)

    /**
     * If this user is local or not
     *
     * @since 2025.12.6.0-SNAPSHOT
     * */
    fun isLocal(): Boolean =
        host == null
}
