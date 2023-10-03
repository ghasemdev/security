package com.example.security.security.keystore

interface KeyManager {
  fun remove(alias: String): Boolean
  fun getAliases(): List<String>
}
