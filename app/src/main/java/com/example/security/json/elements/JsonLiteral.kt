package com.example.security.json.elements

import com.example.security.json.utlis.printQuoted

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
