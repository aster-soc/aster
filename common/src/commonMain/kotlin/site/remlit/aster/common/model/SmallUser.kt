package site.remlit.aster.common.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.js.JsStatic
import kotlin.time.Instant

@JsExport
@Serializable
data class SmallUser(
	val id: String,

	val apId: String,

	val username: String,
	val host: String? = null,
	val displayName: String? = null,

	val avatar: String? = null,
	val avatarAlt: String? = null,

	val automated: Boolean = false,
	val sensitive: Boolean = false,

	val isCat: Boolean = false,

	val createdAt: Instant,
	val updatedAt: Instant? = null
) {
    /**
     * If this user is local or not
     *
     * @since 2025.12.6.0-SNAPSHOT
     * */
    fun isLocal(): Boolean =
        host == null
}
