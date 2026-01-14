package site.remlit.aster.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import site.remlit.aster.model.ap.ApObject
import site.remlit.aster.registry.ApObjectTypeRegistry
import kotlin.reflect.KClass

/**
 * JSON Serialization configuration used for Ktor server and client
 * */
var jsonConfig: Json = Json

@OptIn(ExperimentalSerializationApi::class)
fun setJsonConfig() {
	jsonConfig = Json {
		encodeDefaults = true
		prettyPrint = true
		isLenient = true
		explicitNulls = false
		ignoreUnknownKeys = true

		serializersModule = SerializersModule {
			polymorphic(ApObject::class) {
				for ((klass, serializer) in ApObjectTypeRegistry.apObjectTypes) {
					@Suppress("UNCHECKED_CAST")
					subclass(klass as KClass<ApObject>, serializer as KSerializer<ApObject>)
				}
			}
		}

		// ActivityPub was triggering this by accident, so I disabled this and I made it overly specific.
		classDiscriminator = "@classDiscriminator"
		classDiscriminatorMode = ClassDiscriminatorMode.NONE
	}
}
