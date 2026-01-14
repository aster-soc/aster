package site.remlit.aster.util.serialization

import io.ktor.util.reflect.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import site.remlit.aster.model.ap.ApIdOrObject

object NestedApObjectSerializer : KSerializer<ApIdOrObject> {
	@OptIn(InternalSerializationApi::class)
	override val descriptor: SerialDescriptor =
		buildSerialDescriptor("site.remlit.aster.util.serialization.NestedApObjectSerializer", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: ApIdOrObject) {
		val jsonEncoder = encoder as? JsonEncoder
			?: throw IllegalArgumentException("Only JSON supported")

		when (value) {
			is ApIdOrObject.Id -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
			is ApIdOrObject.Object -> if (value.value.instanceOf(JsonObject::class)) {
				jsonEncoder.encodeJsonElement(
					JsonObject(value.value.jsonObject.toMutableMap().apply {
						this.remove("@context")
					})
				)
			} else {
				jsonEncoder.encodeJsonElement(value.value)
			}
		}
	}

	override fun deserialize(decoder: Decoder): ApIdOrObject = ApIdOrObjectSerializer.deserialize(decoder)
}
