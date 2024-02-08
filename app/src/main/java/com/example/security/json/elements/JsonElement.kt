@file:Suppress("KotlinConstantConditions", "unused")

package com.example.security.json.elements

/**
 * Class representing single JSON element.
 * Can be [JsonPrimitive], [JsonArray] or [JsonObject].
 *
 * [JsonElement.toString] properly prints JSON tree as valid JSON, taking into account quoted values and primitives.
 * Whole hierarchy is serializable, but only when used with Json as [JsonElement] is purely JSON-specific structure,
 * which has a meaningful schemaless semantics only for JSON.
 *
 * The hierarchy is [serializable][Serializable] only by Json format.
 */
sealed class JsonElement

/**
 * Convenience method to get current element as [JsonPrimitive]
 * @throws IllegalArgumentException if current element is not a [JsonPrimitive]
 */
val JsonElement.jsonPrimitive: JsonPrimitive
  get() = this as? JsonPrimitive ?: error("JsonPrimitive")

/**
 * Convenience method to get current element as [JsonObject]
 * @throws IllegalArgumentException if current element is not a [JsonObject]
 */
val JsonElement.jsonObject: JsonObject
  get() = this as? JsonObject ?: error("JsonObject")

/**
 * Convenience method to get current element as [JsonArray]
 * @throws IllegalArgumentException if current element is not a [JsonArray]
 */
val JsonElement.jsonArray: JsonArray
  get() = this as? JsonArray ?: error("JsonArray")

/**
 * Convenience method to get current element as [JsonNull]
 * @throws IllegalArgumentException if current element is not a [JsonNull]
 */
val JsonElement.jsonNull: JsonNull
  get() = this as? JsonNull ?: error("JsonNull")
