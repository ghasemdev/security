package com.example.security.json

data class Entries<K, V>(val value: List<Entry<K, V>>)

fun sortedEntries(vararg entries: Entry<String, Any?>): Entries<String, Any?> {
  val map = entries.toMutableList()
  return Entries(map.sortedBy { it.key })
}
