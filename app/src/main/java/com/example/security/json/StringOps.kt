@file:Suppress("KotlinConstantConditions")

package com.example.security.json

private fun toHexChar(i: Int): Char {
  val d = i and 0xf
  return if (d < 10) (d + '0'.code).toChar()
  else (d - 10 + 'a'.code).toChar()
}

internal val ESCAPE_STRINGS: Array<String?> = arrayOfNulls<String>(93).apply {
  for (c in 0..0x1f) {
    val c1 = toHexChar(c shr 12)
    val c2 = toHexChar(c shr 8)
    val c3 = toHexChar(c shr 4)
    val c4 = toHexChar(c)
    this[c] = "\\u$c1$c2$c3$c4"
  }
  this['"'.code] = "\\\""
  this['\\'.code] = "\\\\"
  this['\t'.code] = "\\t"
  this['\b'.code] = "\\b"
  this['\n'.code] = "\\n"
  this['\r'.code] = "\\r"
  this[0x0c] = "\\f"
}

internal fun StringBuilder.printQuoted(value: String) {
  append(STRING)
  var lastPos = 0
  for (i in value.indices) {
    val c = value[i].code
    if (c < ESCAPE_STRINGS.size && ESCAPE_STRINGS[c] != null) {
      append(value, lastPos, i) // flush prev
      append(ESCAPE_STRINGS[c])
      lastPos = i + 1
    }
  }

  if (lastPos != 0) append(value, lastPos, value.length)
  else append(value)
  append(STRING)
}

/**
 * Returns `true` if the contents of this string is equal to the word "true", ignoring case, `false` if content equals "false",
 * and returns `null` otherwise.
 */
internal fun String.toBooleanStrictOrNull(): Boolean? = when {
  this.equals("true", ignoreCase = true) -> true
  this.equals("false", ignoreCase = true) -> false
  else -> null
}
