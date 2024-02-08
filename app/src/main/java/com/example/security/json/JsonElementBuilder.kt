@file:OptIn(ExperimentalContracts::class)
@file:Suppress("unused")

package com.example.security.json

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
inline fun buildJsonObject(builderAction: JsonObject.Builder.() -> Unit): JsonObject {
  contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
  val builder = JsonObject.Builder()
  builder.builderAction()
  return builder.build()
}

/**
 * Class representing JSON object, consisting of name-value pairs, where value is arbitrary [JsonElement]
 *
 * Since this class also implements [Map] interface, you can use
 * traditional methods like [Map.get] or [Map.getValue] to get Json elements.
 */
class JsonObject(
  private val content: Map<String, JsonElement>
) : JsonElement() {
  override fun equals(other: Any?): Boolean = content == other
  override fun hashCode(): Int = content.hashCode()
  override fun toString(): String {
    return content.entries.joinToString(
      separator = COMMA,
      prefix = BEGIN_OBJ,
      postfix = END_OBJ,
      transform = { (k, v) ->
        buildString {
          printQuoted(k)
          append(COLON)
          append(v)
        }
      }
    )
  }

  /**
   * DSL builder for a [JsonObject]. To create an instance of builder, use [buildJsonObject] build function.
   */
  @JsonDslMarker
  class Builder @PublishedApi internal constructor() {
    private val content: MutableMap<String, JsonElement> = linkedMapOf()

    /**
     * Add the given JSON [element] to a resulting JSON object using the given [key].
     *
     * Returns the previous value associated with [key], or `null` if the key was not present.
     */
    fun put(key: String, element: JsonElement): JsonElement? = content.put(key, element)

    @PublishedApi
    internal fun build(): JsonObject = JsonObject(content)
  }
}

/**
 * Add the [JSON object][JsonObject] produced by the [builderAction] function to a resulting JSON object using the given [key].
 *
 * Returns the previous value associated with [key], or `null` if the key was not present.
 */
fun JsonObject.Builder.putJsonObject(
  key: String,
  builderAction: JsonObject.Builder.() -> Unit
): JsonElement? = put(key, buildJsonObject(builderAction))

///**
// * Add the [JSON array][JsonArray] produced by the [builderAction] function to a resulting JSON object using the given [key].
// *
// * Returns the previous value associated with [key], or `null` if the key was not present.
// */
//fun JsonObject.Builder.putJsonArray(
//  key: String,
//  builderAction: JsonArrayBuilder.() -> Unit
//): JsonElement? =
//  put(key, buildJsonArray(builderAction))

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
@ExperimentalSerializationApi
@Suppress("UNUSED_PARAMETER") // allows to call `put("key", null)`
fun JsonObject.Builder.put(key: String, value: Nothing?): JsonElement? = put(key, JsonNull)

@DslMarker
internal annotation class JsonDslMarker
annotation class ExperimentalSerializationApi

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
 * Class representing JSON array, consisting of indexed values, where value is arbitrary [JsonElement]
 *
 * Since this class also implements [List] interface, you can use
 * traditional methods like [List.get] or [List.getOrNull] to get Json elements.
 */
class JsonArray(
  private val content: List<JsonElement>
) : JsonElement() {
  override fun equals(other: Any?): Boolean = content == other
  override fun hashCode(): Int = content.hashCode()
  override fun toString(): String = content.joinToString(
    prefix = BEGIN_LIST,
    postfix = END_LIST,
    separator = COMMA
  )

  /**
   * DSL builder for a [JsonArray]. To create an instance of builder, use [buildJsonArray] build function.
   */
  @JsonDslMarker
  class Builder @PublishedApi internal constructor() {
    private val content: MutableList<JsonElement> = mutableListOf()

    /**
     * Adds the given JSON [element] to a resulting JSON array.
     *
     * Always returns `true` similarly to [ArrayList] specification.
     */
    fun add(element: JsonElement): Boolean {
      content += element
      return true
    }

    /**
     * Adds the given JSON [elements] to a resulting JSON array.
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    @ExperimentalSerializationApi
    fun addAll(elements: Collection<JsonElement>): Boolean = content.addAll(elements)

    @PublishedApi
    internal fun build(): JsonArray = JsonArray(content)
  }
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
@ExperimentalSerializationApi
@Suppress("UNUSED_PARAMETER") // allows to call `add(null)`
fun JsonArray.Builder.add(value: Nothing?): Boolean = add(JsonNull)

/**
 * Adds the [JSON object][JsonObject] produced by the [builderAction] function to a resulting JSON array.
 *
 * Always returns `true` similarly to [ArrayList] specification.
 */
fun JsonArray.Builder.addJsonObject(builderAction: JsonObject.Builder.() -> Unit): Boolean =
  add(buildJsonObject(builderAction))

/**
 * Adds the [JSON array][JsonArray] produced by the [builderAction] function to a resulting JSON array.
 *
 * Always returns `true` similarly to [ArrayList] specification.
 */
fun JsonArray.Builder.addJsonArray(builderAction: JsonArray.Builder.() -> Unit): Boolean =
  add(buildJsonArray(builderAction))

/**
 * Adds the given string [values] to a resulting JSON array.
 *
 * @return `true` if the list was changed as the result of the operation.
 */
@JvmName("addAllStrings")
@ExperimentalSerializationApi
fun JsonArray.Builder.addAll(values: Collection<String?>): Boolean =
  addAll(values.map(::JsonPrimitive))

/**
 * Adds the given boolean [values] to a resulting JSON array.
 *
 * @return `true` if the list was changed as the result of the operation.
 */
@JvmName("addAllBooleans")
@ExperimentalSerializationApi
fun JsonArray.Builder.addAll(values: Collection<Boolean?>): Boolean =
  addAll(values.map(::JsonPrimitive))

/**
 * Adds the given numeric [values] to a resulting JSON array.
 *
 * @return `true` if the list was changed as the result of the operation.
 */
@JvmName("addAllNumbers")
@ExperimentalSerializationApi
fun JsonArray.Builder.addAll(values: Collection<Number?>): Boolean =
  addAll(values.map(::JsonPrimitive))
