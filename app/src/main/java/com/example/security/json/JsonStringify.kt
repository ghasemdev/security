package com.example.security.json

fun <K, V> Entries<K, V>.jsonStringify(): String = buildString {
  appendJsonObjectStarter()
  this@jsonStringify.value.forEachIndexed { index, entries ->
    appendData(entries.key)
    appendKeyValueSeparator()
    appendData(entries.value)
    if (index != this@jsonStringify.value.size - 1) {
      appendItemSeparator()
    }
  }
  appendJsonObjectEnder()
}

private fun <T> StringBuilder.appendData(data: T) {
  when (data) {
    is String -> {
      appendString(data)
    }

    is Iterable<*> -> {
      appendIterable(data)
    }

    is Array<*> -> {
      appendArray(data)
    }

    is Entries<*, *> -> {
      append(data.jsonStringify())
    }

    else -> {
      append(data)
    }
  }
}

private fun StringBuilder.appendIterable(value: Iterable<*>) {
  appendJsonArrayStarter()
  value.forEachIndexed { index, item ->
    appendData(item)
    if (index != value.count() - 1) {
      appendItemSeparator()
    }
  }
  appendJsonArrayEnder()
}

private fun StringBuilder.appendArray(value: Array<*>) {
  appendJsonArrayStarter()
  value.forEachIndexed { index, item ->
    appendData(item)
    if (index != value.count() - 1) {
      appendItemSeparator()
    }
  }
  appendJsonArrayEnder()
}

@Suppress("NOTHING_TO_INLINE")
private inline fun StringBuilder.appendString(value: String) {
  append("\"")
  append(value)
  append("\"")
}

@Suppress("NOTHING_TO_INLINE")
private inline fun StringBuilder.appendKeyValueSeparator(): StringBuilder = append(":")

@Suppress("NOTHING_TO_INLINE")
private inline fun StringBuilder.appendItemSeparator(): StringBuilder = append(",")

@Suppress("NOTHING_TO_INLINE")
private inline fun StringBuilder.appendJsonObjectStarter(): StringBuilder = append("{")

@Suppress("NOTHING_TO_INLINE")
private inline fun StringBuilder.appendJsonObjectEnder(): StringBuilder = append("}")

@Suppress("NOTHING_TO_INLINE")
private inline fun StringBuilder.appendJsonArrayStarter(): StringBuilder = append("[")

@Suppress("NOTHING_TO_INLINE")
private inline fun StringBuilder.appendJsonArrayEnder(): StringBuilder = append("]")
