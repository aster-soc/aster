package site.remlit.aster.model.ap

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import site.remlit.aster.util.jsonConfig
import site.remlit.aster.util.serialization.ApIdOrObjectSerializer

@Serializable(with = ApIdOrObjectSerializer::class)
sealed class ApIdOrObject {
	@Serializable
	data class Id(val value: String) : ApIdOrObject()

	@Serializable
	data class Object(
		@Polymorphic val value: JsonElement,
	) : ApIdOrObject()

	companion object {
		fun createObject(json: JsonElement) = Object(json)

		inline fun <reified T> createObject(obj: () -> T) = createObject(
			jsonConfig.encodeToJsonElement<T>(obj()) as JsonObject
		)

		inline fun <reified T> createArray(obj: () -> T) = createObject(
			jsonConfig.encodeToJsonElement<T>(obj()) as JsonArray
		)
	}
}
