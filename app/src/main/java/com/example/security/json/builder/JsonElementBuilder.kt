@file:OptIn(ExperimentalContracts::class)
@file:Suppress("unused")

package com.example.security.json.builder

import com.example.security.json.annotation.ExperimentalJsonApi
import com.example.security.json.elements.JsonArray
import com.example.security.json.elements.JsonElement
import com.example.security.json.elements.JsonNull
import com.example.security.json.elements.JsonObject
import com.example.security.json.elements.JsonPrimitive
import com.example.security.json.utlis.JsonSortStrategy
import com.example.security.json.utlis.JsonSortStrategy.ALPHABET_ASCENDING
import com.example.security.json.utlis.JsonSortStrategy.NONE
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Builds [JsonObject] with the given [builderAction] builder.
 * Example of usage:
 * ```
 * val json = buildJsonObject {
 *     put("booleanKey", true)
 *     putJsonArray("arrayKey") {
 *         for (i in 1..10) add(i)
 *     }
 *     putJsonObject("objectKey") {
 *         put("stringKey", "stringValue")
 *     }
 * }
 * ```
 */
inline fun buildJsonObject(
  sortBy: JsonSortStrategy = NONE,
  builderAction: JsonObject.Builder.() -> Unit
): JsonObject {
  contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
  val builder = JsonObject.Builder()
  builder.builderAction()
  return builder.build(sortBy)
}

/**
 * Builds sorted [JsonObject] with the given [builderAction] builder.
 * Example of usage:
 * ```
 * val json = buildSortedJsonObject {
 *     put("booleanKey", true)
 *     putJsonArray("arrayKey") {
 *         for (i in 1..10) add(i)
 *     }
 *     putSortedJsonObject("objectKey") {
 *         put("stringKey", "stringValue")
 *     }
 * }
 * ```
 */
inline fun buildSortedJsonObject(
  builderAction: JsonObject.Builder.() -> Unit
): JsonObject {
  contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
  val builder = JsonObject.Builder()
  builder.builderAction()
  return builder.build(ALPHABET_ASCENDING)
}

/**
 * Add the [JsonObject] produced by the [builderAction] function to a resulting JSON object using the given [key].
 *
 * Returns the previous value associated with [key], or `null` if the key was not present.
 */
fun JsonObject.Builder.putJsonObject(
  key: String,
  sortBy: JsonSortStrategy = NONE,
  builderAction: JsonObject.Builder.() -> Unit
): JsonElement? = put(key, buildJsonObject(sortBy, builderAction))

/**
 * Add the sorted [JsonObject] produced by the [builderAction] function to a resulting JSON object using the given [key].
 *
 * Returns the previous value associated with [key], or `null` if the key was not present.
 */
fun JsonObject.Builder.putSortedJsonObject(
  key: String,
  builderAction: JsonObject.Builder.() -> Unit
): JsonElement? = put(key, buildJsonObject(ALPHABET_ASCENDING, builderAction))

/**
 * Add the [JsonArray] produced by the [builderAction] function to a resulting JSON object using the given [key].
 *
 * Returns the previous value associated with [key], or `null` if the key was not present.
 */
fun JsonObject.Builder.putJsonArray(
  key: String,
  builderAction: JsonArray.Builder.() -> Unit
): JsonElement? = put(key, buildJsonArray(builderAction))

/**
 * Add the given boolean [value] to a resulting JSON object using the given [key].
 *
 * Returns the previous value associated with [key], or `null` if the key was not present.
 */
fun JsonObject.Builder.put(key: String, value: Boolean?): JsonElement? =
  put(key, JsonPrimitive(value))

/**
 * Add the given numeric [value] to a resulting JSON object using the given [key].
 *
 * Returns the previous value associated with [key], or `null` if the key was not present.
 */
fun JsonObject.Builder.put(key: String, value: Number?): JsonElement? =
  put(key, JsonPrimitive(value))

/**
 * Add the given string [value] to a resulting JSON object using the given [key].
 *
 * Returns the previous value associated with [key], or `null` if the key was not present.
 */
fun JsonObject.Builder.put(key: String, value: String?): JsonElement? =
  put(key, JsonPrimitive(value))

/**
 * Add `null` to a resulting JSON object using the given [key].
 *
 * Returns the previous value associated with [key], or `null` if the key was not present.
 */
@ExperimentalJsonApi
@Suppress("UNUSED_PARAMETER") // allows to call `put(“key”, null)`
fun JsonObject.Builder.put(key: String, value: Nothing?): JsonElement? = put(key, JsonNull)

/**
 * Builds [JsonArray] with the given [builderAction] builder.
 * Example of usage:
 * ```
 * val json = buildJsonArray {
 *     add(true)
 *     addJsonArray {
 *         for (i in 1..10) add(i)
 *     }
 *     addJsonObject {
 *         put("stringKey", "stringValue")
 *     }
 * }
 * ```
 */
inline fun buildJsonArray(builderAction: JsonArray.Builder.() -> Unit): JsonArray {
  contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
  val builder = JsonArray.Builder()
  builder.builderAction()
  return builder.build()
}

/**
 * Adds the given boolean [value] to a resulting JSON array.
 *
 * Always returns `true` similarly to [ArrayList] specification.
 */
fun JsonArray.Builder.add(value: Boolean?): Boolean = add(JsonPrimitive(value))

/**
 * Adds the given numeric [value] to a resulting JSON array.
 *
 * Always returns `true` similarly to [ArrayList] specification.
 */
fun JsonArray.Builder.add(value: Number?): Boolean = add(JsonPrimitive(value))

/**
 * Adds the given string [value] to a resulting JSON array.
 *
 * Always returns `true` similarly to [ArrayList] specification.
 */
fun JsonArray.Builder.add(value: String?): Boolean = add(JsonPrimitive(value))

/**
 * Adds `null` to a resulting JSON array.
 *
 * Always returns `true` similarly to [ArrayList] specification.
 */
@ExperimentalJsonApi
@Suppress("UNUSED_PARAMETER") // allows to call `add(null)`
fun JsonArray.Builder.add(value: Nothing?): Boolean = add(JsonNull)

/**
 * Adds the [JsonObject] produced by the [builderAction] function to a resulting JSON array.
 *
 * Always returns `true` similarly to [ArrayList] specification.
 */
fun JsonArray.Builder.addJsonObject(
  sortBy: JsonSortStrategy = NONE,
  builderAction: JsonObject.Builder.() -> Unit
): Boolean = add(buildJsonObject(sortBy, builderAction))

/**
 * Adds the sorted [JsonObject] produced by the [builderAction] function to a resulting JSON array.
 *
 * Always returns `true` similarly to [ArrayList] specification.
 */
fun JsonArray.Builder.addSortedJsonObject(
  builderAction: JsonObject.Builder.() -> Unit
): Boolean = add(buildJsonObject(ALPHABET_ASCENDING, builderAction))

/**
 * Adds the [JsonArray] produced by the [builderAction] function to a resulting JSON array.
 *
 * Always returns `true` similarly to [ArrayList] specification.
 */
fun JsonArray.Builder.addJsonArray(
  builderAction: JsonArray.Builder.() -> Unit
): Boolean = add(buildJsonArray(builderAction))

/**
 * Adds the given string [values] to a resulting JSON array.
 *
 * @return `true` if the list was changed as the result of the operation.
 */
@JvmName("addAllStrings")
@ExperimentalJsonApi
fun JsonArray.Builder.addAll(values: Collection<String?>): Boolean =
  addAll(values.map(::JsonPrimitive))

/**
 * Adds the given boolean [values] to a resulting JSON array.
 *
 * @return `true` if the list was changed as the result of the operation.
 */
@JvmName("addAllBooleans")
@ExperimentalJsonApi
fun JsonArray.Builder.addAll(values: Collection<Boolean?>): Boolean =
  addAll(values.map(::JsonPrimitive))

/**
 * Adds the given numeric [values] to a resulting JSON array.
 *
 * @return `true` if the list was changed as the result of the operation.
 */
@JvmName("addAllNumbers")
@ExperimentalJsonApi
fun JsonArray.Builder.addAll(values: Collection<Number?>): Boolean =
  addAll(values.map(::JsonPrimitive))

/**
 * Adds the given string [values] to a resulting JSON array.
 *
 * @return `true` if the list was changed as the result of the operation.
 */
@JvmName("addAllStrings")
@ExperimentalJsonApi
fun JsonArray.Builder.addAll(vararg values: String?): Boolean =
  addAll(values.map(::JsonPrimitive))

/**
 * Adds the given boolean [values] to a resulting JSON array.
 *
 * @return `true` if the list was changed as the result of the operation.
 */
@JvmName("addAllBooleans")
@ExperimentalJsonApi
fun JsonArray.Builder.addAll(vararg values: Boolean?): Boolean =
  addAll(values.map(::JsonPrimitive))

/**
 * Adds the given numeric [values] to a resulting JSON array.
 *
 * @return `true` if the list was changed as the result of the operation.
 */
@JvmName("addAllNumbers")
@ExperimentalJsonApi
fun JsonArray.Builder.addAll(vararg values: Number?): Boolean =
  addAll(values.map(::JsonPrimitive))
