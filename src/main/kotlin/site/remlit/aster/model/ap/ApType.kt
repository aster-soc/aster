package site.remlit.aster.model.ap

import kotlinx.serialization.Serializable

@Serializable
class ApType {
	enum class Activity {
		Accept,
		Add,
		Announce,
		Bite,
		Block,
		Create,
		Delete,
		EmojiReact,
		Follow,
		Like,
		Reject,
		Remove,
		Undo,
		Update
	}

	enum class Tag {
		Emoji,
		Hashtag,
		Mention
	}

	enum class Object {
		Person,
		Service,
		Note,
		Image,
		Key,
		Tombstone,
		Document,
		Collection,
		OrderedCollection,
		UnorderedCollection,
	}
}
