package com.example.security.json.elements

import com.example.security.json.annotation.ExperimentalJsonApi
import com.example.security.json.annotation.JsonDslMarker
import com.example.security.json.builder.buildJsonArray
import com.example.security.json.constants.BEGIN_LIST
import com.example.security.json.constants.COMMA
import com.example.security.json.constants.END_LIST

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
    @ExperimentalJsonApi
    fun addAll(elements: Collection<JsonElement>): Boolean = content.addAll(elements)

    @PublishedApi
    internal fun build(): JsonArray = JsonArray(content)
  }
}
