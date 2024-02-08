package com.example.security

import com.example.security.json.builder.buildSortedJsonObject
import com.example.security.json.builder.put
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class BuildJsonTest {
  @Test
  fun buildJsonObject() {
    val json = buildSortedJsonObject {
      put("hi1", "h")
      put("hi", 2)
    }
    assert(json.toString() == jsonTest)
  }
}

private const val jsonTest = """{"hi":2,"hi1":"h"}"""
