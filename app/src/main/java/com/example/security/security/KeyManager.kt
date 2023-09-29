package com.example.security.security

import java.security.KeyStore
import javax.crypto.SecretKey

interface KeyManager {
  fun remove(alias: String): Boolean
  fun getAliases(): List<String>
}

interface SecretKeyManager : KeyManager {
  fun getOrGenerateSecretKey(alias: String): SecretKey
  fun generateSecretKey(alias: String): SecretKey
  fun getSecretKey(alias: String): SecretKey?
}

internal val keystore: KeyStore by lazy {
  KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
    load(null)
  }
}

private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
