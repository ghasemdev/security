package com.example.security.json.elements

import com.example.security.json.constants.NULL

/**
 * Class representing JSON `null` value
 */
object JsonNull : JsonPrimitive() {
  override val isString: Boolean get() = false
  override val content: String = NULL
}
