package site.remlit.aster.common.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extracts a string from a JsonElement
 *
 * @return Extracted string or null
 * */
inline fun extractString(item: () -> JsonElement?): String? =
	when (val item = item()) {
		is JsonArray -> item.first().jsonPrimitive.toString()
		is JsonPrimitive -> item.jsonPrimitive.contentOrNull
		else -> null
	}

/**
 * Extracts a boolean from a JsonElement
 *
 * @return Extracted boolean or null
 * */
inline fun extractBoolean(item: () -> JsonElement?): Boolean? =
	when (val item = item()) {
		is JsonArray -> item.first().jsonPrimitive.booleanOrNull
		is JsonPrimitive -> item.jsonPrimitive.booleanOrNull
		else -> null
	}

/**
 * Extracts a JsonArray from a JsonElement
 *
 * @return Extracted JsonArray or null
 * */
inline fun extractArray(item: () -> JsonElement?): JsonArray? = item() as? JsonArray

/**
 * Extracts a JsonObject from a JsonElement
 *
 * @return Extracted JsonObject or null
 * */
inline fun extractObject(item: () -> JsonElement?): JsonObject? = item()?.jsonObject
