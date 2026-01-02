package site.remlit.aster.model.ap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import site.remlit.aster.common.util.toLocalInstant
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.service.ap.ApIdService
import kotlin.time.Instant

/**
 * ActivityPub representation of User
 * Only to be used on local users (where host is null)
 * */
@Serializable
data class ApActor(
	val id: String,
	val type: ApType.Object = ApType.Object.Person,
	val url: String? = null,

	val preferredUsername: String,
	val name: String? = null,

	val icon: ApImage? = null,
	val image: ApImage? = null,

	val summary: String? = null,
	@SerialName("_misskey_summary")
	val misskeySummary: String? = null,

	val sensitive: Boolean = false,
	val discoverable: Boolean = false,
	val manuallyApprovesFollowers: Boolean = false,
	val isCat: Boolean = false,
	val speakAsCat: Boolean = false,

	@SerialName("vcard:bday")
	val vcardBday: String? = null,
	@SerialName("vcard:Address")
	val vcardAddress: String? = null,

	val published: Instant,

	val inbox: String,
	val outbox: String? = null,
	val sharedInbox: String,
	// endpoints

	// followers & following

	val publicKey: ApKey

) : ApObjectWithContext() {
	companion object {
		fun fromEntity(user: UserEntity): ApActor =
			ApActor(
				id = user.apId,
				type = ApType.Object.Person,
				preferredUsername = user.username,
				name = user.displayName,

				icon = if (user.avatar != null) ApImage(
					url = user.avatar!!,
					sensitive = user.sensitive,
					name = user.avatarAlt,
					summary = user.avatarAlt
				) else null,
				image = if (user.banner != null) ApImage(
					url = user.banner!!,
					sensitive = user.sensitive,
					name = user.bannerAlt,
					summary = user.bannerAlt
				) else null,

				summary = user.bio,
				misskeySummary = user.bio,

				sensitive = user.sensitive,
				discoverable = user.discoverable,
				manuallyApprovesFollowers = user.locked,
				isCat = user.isCat,
				speakAsCat = user.isCat,

				vcardBday = user.birthday,
				vcardAddress = user.location,

				published = user.createdAt.toLocalInstant(),

				inbox = user.inbox,
				outbox = user.outbox,

				sharedInbox = ApIdService.renderInboxApId(),

				publicKey = ApKey(
					id = user.apId + "#main-key",
					owner = user.apId,
					publicKeyPem = user.publicKey
				)
			)
	}
}
