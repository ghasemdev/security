package com.example.security.json.elements

import com.example.security.json.annotation.JsonDslMarker
import com.example.security.json.builder.buildJsonObject
import com.example.security.json.constants.BEGIN_OBJ
import com.example.security.json.constants.COLON
import com.example.security.json.constants.COMMA
import com.example.security.json.constants.END_OBJ
import com.example.security.json.utlis.JsonSortStrategy
import com.example.security.json.utlis.printQuoted

/**
 * Class representing JSON object, consisting of name-value pairs, where value is arbitrary [JsonElement]
 *
 * Since this class also implements [Map] interface, you can use
 * traditional methods like [Map.get] or [Map.getValue] to get Json elements.
 */
class JsonObject(
  private val content: List<Pair<String, JsonElement>>,
  private val sortStrategy: JsonSortStrategy
) : JsonElement() {
  override fun equals(other: Any?): Boolean = content == other
  override fun hashCode(): Int = content.hashCode()
  override fun toString(): String {
    return (when (sortStrategy) {
      JsonSortStrategy.NONE -> content
      JsonSortStrategy.HASH_CODE -> content.sortedBy { it.hashCode() }
      JsonSortStrategy.ALPHABET_ASCENDING -> content.sortedBy { it.first }
      JsonSortStrategy.ALPHABET_DESCENDING -> content.sortedByDescending { it.first }
    }).joinToString(
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
    private val content: MutableList<Pair<String, JsonElement>> = mutableListOf()

    /**
     * Add the given JSON [element] to a resulting JSON object using the given [key].
     *
     * Returns the previous value associated with [key], or `null` if the key was not present.
     */
    fun put(key: String, element: JsonElement): JsonElement? {
      val pair = Pair(key, element)
      val indexOf = content.indexOfFirst { it.first == key }

      return if (indexOf == -1) { // New JsonElement
        content.add(pair)
        null
      } else {
        val previousElement = content[indexOf]
        content.removeAt(indexOf)
        content.add(indexOf, pair)
        previousElement.second
      }
    }

    @PublishedApi
    internal fun build(sortBy: JsonSortStrategy): JsonObject = JsonObject(content, sortBy)
  }
}
