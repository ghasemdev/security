@file:Suppress("KotlinConstantConditions", "unused")

package com.example.security.json

import kotlinx.serialization.SerializationException

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
 * Class representing JSON primitive value.
 * JSON primitives include numbers, strings, booleans and special null value [JsonNull].
 */
sealed class JsonPrimitive : JsonElement() {
  /**
   * Indicates whether the primitive was explicitly constructed from [String] and
   * whether it should be serialized as one. E.g. `JsonPrimitive("42")` is represented
   * by a string, while `JsonPrimitive(42)` is not.
   * These primitives will be serialized as `42` and `"42"` respectively.
   */
  abstract val isString: Boolean

  /**
   * Content of given element without quotes. For [JsonNull] these methods returns `null`
   */
  abstract val content: String

  override fun toString(): String = content
}

/** Creates a [JsonPrimitive] from the given boolean. */
fun JsonPrimitive(value: Boolean?): JsonPrimitive {
  if (value == null) return JsonNull
  return JsonLiteral(value, isString = false)
}

/** Creates a [JsonPrimitive] from the given number. */
fun JsonPrimitive(value: Number?): JsonPrimitive {
  if (value == null) return JsonNull
  return JsonLiteral(value, isString = false)
}

/**
 * Creates a numeric [JsonPrimitive] from the given [UByte].
 *
 * The value will be encoded as a JSON number.
 */
@ExperimentalSerializationApi
fun JsonPrimitive(value: UByte): JsonPrimitive = JsonPrimitive(value.toULong())

/**
 * Creates a numeric [JsonPrimitive] from the given [UShort].
 *
 * The value will be encoded as a JSON number.
 */
@ExperimentalSerializationApi
fun JsonPrimitive(value: UShort): JsonPrimitive = JsonPrimitive(value.toULong())

/**
 * Creates a numeric [JsonPrimitive] from the given [UInt].
 *
 * The value will be encoded as a JSON number.
 */
@ExperimentalSerializationApi
fun JsonPrimitive(value: UInt): JsonPrimitive = JsonPrimitive(value.toULong())

/**
 * Creates a numeric [JsonPrimitive] from the given [ULong].
 *
 * The value will be encoded as a JSON number.
 */
@ExperimentalSerializationApi
fun JsonPrimitive(value: ULong): JsonPrimitive = JsonUnquotedLiteral(value.toString())

/** Creates a [JsonPrimitive] from the given string. */
fun JsonPrimitive(value: String?): JsonPrimitive {
  if (value == null) return JsonNull
  return JsonLiteral(value, isString = true)
}

/** Creates [JsonNull]. */
@ExperimentalSerializationApi
@Suppress("FunctionName", "UNUSED_PARAMETER") // allows to call `JsonPrimitive(null)`
fun JsonPrimitive(value: Nothing?): JsonNull = JsonNull

/**
 * Creates a [JsonPrimitive] from the given string, without surrounding it in quotes.
 *
 * This function is provided for encoding raw JSON values that cannot be encoded using the [JsonPrimitive] functions.
 * For example,
 *
 * * precise numeric values (avoiding floating-point precision errors associated with [Double] and [Float]),
 * * large numbers,
 * * or complex JSON objects.
 *
 * Be aware that it is possible to create invalid JSON using this function.
 *
 * Creating a literal unquoted value of `null` (as in, `value == "null"`) is forbidden. If you want to create
 * JSON null literal, use [JsonNull] object, otherwise, use [JsonPrimitive].
 *
 * @see JsonPrimitive is the preferred method for encoding JSON primitives.
 * @throws JsonEncodingException if `value == "null"`
 */
@ExperimentalSerializationApi
@Suppress("FunctionName")
fun JsonUnquotedLiteral(value: String?): JsonPrimitive {
  return when (value) {
    null -> JsonNull
    JsonNull.content -> throw JsonEncodingException("Creating a literal unquoted value of 'null' is forbidden. If you want to create JSON null literal, use JsonNull object, otherwise, use JsonPrimitive.")
    else -> JsonLiteral(value, isString = false)
  }
}

/**
 * Generic exception indicating a problem with JSON serialization and deserialization.
 */
internal open class JsonException(message: String) : SerializationException(message)

/**
 * Thrown when Json has didn't create a JSON string from the given value.
 */
internal class JsonEncodingException(message: String) : JsonException(message)

internal class JsonLiteral internal constructor(
  body: Any,
  override val isString: Boolean,
) : JsonPrimitive() {
  override val content: String = body.toString()

  override fun toString(): String =
    if (isString) buildString { printQuoted(content) }
    else content

  // Compare by `content` and `isString`, because body can be kotlin.Long=42 or kotlin.String="42"
  @Suppress("RedundantIf")
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as JsonLiteral
    if (isString != other.isString) return false
    if (content != other.content) return false
    return true
  }

  override fun hashCode(): Int {
    var result = isString.hashCode()
    result = 31 * result + content.hashCode()
    return result
  }
}

/**
 * Class representing JSON `null` value
 */
object JsonNull : JsonPrimitive() {
  override val isString: Boolean get() = false
  override val content: String = NULL
}

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

/**
 * Returns content of the current element as int
 * @throws NumberFormatException if current element is not a valid representation of number
 */
val JsonPrimitive.int: Int get() = content.toInt()

/**
 * Returns content of the current element as int or `null` if current element is not a valid representation of number
 */
val JsonPrimitive.intOrNull: Int? get() = content.toIntOrNull()

/**
 * Returns content of current element as long
 * @throws NumberFormatException if current element is not a valid representation of number
 */
val JsonPrimitive.long: Long get() = content.toLong()

/**
 * Returns content of current element as long or `null` if current element is not a valid representation of number
 */
val JsonPrimitive.longOrNull: Long? get() = content.toLongOrNull()

/**
 * Returns content of current element as double
 * @throws NumberFormatException if current element is not a valid representation of number
 */
val JsonPrimitive.double: Double get() = content.toDouble()

/**
 * Returns content of current element as double or `null` if current element is not a valid representation of number
 */
val JsonPrimitive.doubleOrNull: Double? get() = content.toDoubleOrNull()

/**
 * Returns content of current element as float
 * @throws NumberFormatException if current element is not a valid representation of number
 */
val JsonPrimitive.float: Float get() = content.toFloat()

/**
 * Returns content of current element as float or `null` if current element is not a valid representation of number
 */
val JsonPrimitive.floatOrNull: Float? get() = content.toFloatOrNull()

/**
 * Returns content of current element as boolean
 * @throws IllegalStateException if current element doesn't represent boolean
 */
val JsonPrimitive.boolean: Boolean
  get() = content.toBooleanStrictOrNull()
    ?: throw IllegalStateException("$this does not represent a Boolean")

/**
 * Returns content of current element as boolean or `null` if current element is not a valid representation of boolean
 */
val JsonPrimitive.booleanOrNull: Boolean? get() = content.toBooleanStrictOrNull()

/**
 * Content of the given element without quotes or `null` if current element is [JsonNull]
 */
val JsonPrimitive.contentOrNull: String? get() = if (this is JsonNull) null else content

private fun JsonElement.error(element: String): Nothing =
  throw IllegalArgumentException("Element ${this::class} is not a $element")
