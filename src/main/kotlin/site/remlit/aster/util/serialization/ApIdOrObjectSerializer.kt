package site.remlit.aster.util.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import site.remlit.aster.model.ap.ApIdOrObject

object ApIdOrObjectSerializer : KSerializer<ApIdOrObject> {

	@OptIn(InternalSerializationApi::class)
	override val descriptor =
		buildSerialDescriptor("ApIdOrObject", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: ApIdOrObject) {
		val jsonEncoder = encoder as? JsonEncoder
			?: throw IllegalArgumentException("Only JSON supported")

		when (value) {
			is ApIdOrObject.Id -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
			is ApIdOrObject.Object -> jsonEncoder.encodeJsonElement(
				jsonEncoder.json.encodeToJsonElement(value.value)
			)
		}
	}

	override fun deserialize(decoder: Decoder): ApIdOrObject {
		val jsonDecoder = decoder as? JsonDecoder
			?: throw IllegalArgumentException("Only JSON supported")

		return when (val element = jsonDecoder.decodeJsonElement()) {
			is JsonPrimitive if element.isString -> ApIdOrObject.Id(element.content)
			is JsonObject -> ApIdOrObject.Object(
				jsonDecoder.json.decodeFromJsonElement<JsonObject>(element)
			)

			else -> throw IllegalArgumentException("Unknown JSON element for ApIdOrObject: $element")
		}
	}
}
