package com.example.security.security.keystore

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.security.security.keystore.SecretKeyManager.Companion.ALGORITHM
import com.example.security.security.keystore.SecretKeyManager.Companion.BLOCK_MODE
import com.example.security.security.keystore.SecretKeyManager.Companion.PADDING
import com.example.security.security.keystore.internal.keystore
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SecretKeyManagerImpl(
  private val isSupportStrongBox: Boolean
) : SecretKeyManager {
  override fun getOrGenerateSecretKey(alias: String): SecretKey {
    return getSecretKey(alias) ?: generateSecretKey(alias)
  }

  override fun generateSecretKey(alias: String): SecretKey {
    return KeyGenerator
      .getInstance(ALGORITHM)
      .apply {
        init(
          KeyGenParameterSpec
            .Builder(alias, ENCRYPTION_PURPOSE)
            .setKeySize(KEY_SIZE)
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false)
            .apply {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (isSupportStrongBox) {
                  setIsStrongBoxBacked(true)
                }
              }
            }
            .build()
        )
      }
      .generateKey()
  }

  override fun getSecretKey(alias: String): SecretKey? {
    val entry = keystore.getEntry(alias, null) as? SecretKeyEntry
    return entry?.secretKey
  }

  override fun remove(alias: String): Boolean {
    return try {
      keystore.deleteEntry(alias)
      true
    } catch (_: Exception) {
      false
    }
  }

  override fun getAliases(): List<String> = keystore.aliases().toList()

  private companion object {
    const val ENCRYPTION_PURPOSE = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    const val KEY_SIZE = 256
  }
}
