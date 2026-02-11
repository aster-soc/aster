package site.remlit.aster.registry

import kotlinx.serialization.KSerializer
import org.jetbrains.annotations.ApiStatus
import org.slf4j.LoggerFactory
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.ap.ApActor
import site.remlit.aster.model.ap.ApNote
import site.remlit.aster.model.ap.ApObject
import site.remlit.aster.model.ap.ApTypedObject
import kotlin.reflect.KClass

object ApObjectTypeRegistry {
	private val logger = LoggerFactory.getLogger(ApObjectTypeRegistry::class.java)

	@JvmStatic
	val apObjectTypes =
		mutableListOf<Pair<KClass<out ApObject>, KSerializer<out ApObject>>>()

	/**
	 * If you have an ApObject that may be an `object` property on an activity, you need to register it here.
	 *
	 * @param klass Class of object type
	 * @param serializer Serializer of object type
	 * */
	@JvmStatic
	fun register(klass: KClass<out ApObject>, serializer: KSerializer<out ApObject>) {
		apObjectTypes.add(Pair(klass, serializer))
		logger.debug("Added ${klass.simpleName} as an AP object type")
	}

	/**
	 * If you have an ApObject that may be an `object` property on an activity, you need to register it here.
	 *
	 * @param serializer Serializer of object type
	 * */
	@JvmSynthetic
	inline fun <reified T : ApObject> register(
		serializer: KSerializer<out ApObject>
	) = register(T::class, serializer)

	/**
	 * Register internal types.
	 * */
	@ApiStatus.Internal
	fun registerInternal() {
		register<ApTypedObject>(ApTypedObject.serializer())

		register<ApActor>(ApActor.serializer())
		register<ApNote>(ApNote.serializer())
	}
}
