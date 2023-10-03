package com.example.security.security.keystore

import android.security.keystore.KeyProperties
import javax.crypto.SecretKey

interface SecretKeyManager : KeyManager {
  fun getOrGenerateSecretKey(alias: String): SecretKey
  fun generateSecretKey(alias: String): SecretKey
  fun getSecretKey(alias: String): SecretKey?

  companion object {
    const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
  }
}
