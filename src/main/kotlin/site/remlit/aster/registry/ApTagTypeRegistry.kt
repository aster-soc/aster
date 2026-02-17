package site.remlit.aster.registry

import kotlinx.serialization.KSerializer
import org.jetbrains.annotations.ApiStatus
import org.slf4j.LoggerFactory
import site.remlit.aster.model.ap.ApTag
import site.remlit.aster.model.ap.tag.ApEmojiTag
import site.remlit.aster.model.ap.tag.ApMentionTag
import kotlin.reflect.KClass

object ApTagTypeRegistry {
	private val logger = LoggerFactory.getLogger(ApTagTypeRegistry::class.java)

	@JvmStatic
	val apTagTypes =
		mutableListOf<Pair<KClass<out ApTag>, KSerializer<out ApTag>>>()

	/**
	 * If you have an ApTag subclass, you need to register it here.
	 *
	 * @param klass Class of tag type
	 * @param serializer Serializer of tag type
	 * */
	@JvmStatic
	fun register(klass: KClass<out ApTag>, serializer: KSerializer<out ApTag>) {
		apTagTypes.add(Pair(klass, serializer))
		logger.debug("Added ${klass.simpleName} as an AP tag type")
	}

	/**
	 * If you have an ApTag subclass, you need to register it here.
	 *
	 * @param serializer Serializer of tag type
	 * */
	@JvmSynthetic
	inline fun <reified T : ApTag> register(
		serializer: KSerializer<out ApTag>
	) = register(T::class, serializer)

	/**
	 * Register internal types.
	 * */
	@ApiStatus.Internal
	fun registerInternal() {
		register<ApEmojiTag>(ApEmojiTag.serializer())
		register<ApMentionTag>(ApMentionTag.serializer())
	}
}

