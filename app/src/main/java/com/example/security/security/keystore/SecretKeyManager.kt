package com.example.security.security.keystore

import javax.crypto.SecretKey

interface SecretKeyManager : KeyManager {
  fun getOrGenerateSecretKey(alias: String): SecretKey
  fun generateSecretKey(alias: String): SecretKey
  fun getSecretKey(alias: String): SecretKey?
}
