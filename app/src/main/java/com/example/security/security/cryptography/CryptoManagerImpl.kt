package com.example.security.security.cryptography

import com.example.security.security.keystore.SecretKeyManager
import com.example.security.security.keystore.SecretKeyManager.Companion.ALGORITHM
import com.example.security.security.keystore.SecretKeyManager.Companion.BLOCK_MODE
import com.example.security.security.keystore.SecretKeyManager.Companion.PADDING
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class CryptoManagerImpl(
  private val keyManager: SecretKeyManager
) : CryptoManager {
  private fun encryptCipher() = Cipher
    .getInstance(TRANSFORMATION).apply {
      init(Cipher.ENCRYPT_MODE, keyManager.getOrGenerateSecretKey(ALIAS_KEY))
    }

  private fun decryptCipher(tagLength: Int, iv: ByteArray) = Cipher
    .getInstance(TRANSFORMATION)
    .apply {
      init(Cipher.DECRYPT_MODE, keyManager.getSecretKey(ALIAS_KEY), GCMParameterSpec(tagLength, iv))
    }

  override fun encrypt(plaintext: ByteArray): Triple<ByteArray, Int, ByteArray> {
    val encryptCipher = encryptCipher()
    val gcmParameter = encryptCipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
    return Triple(encryptCipher.doFinal(plaintext), gcmParameter.tLen, gcmParameter.iv)
  }

  override fun decrypt(ciphertext: ByteArray, tagLength: Int, iv: ByteArray): ByteArray =
    decryptCipher(tagLength, iv).doFinal(ciphertext)

  companion object {
    private const val ALIAS_KEY = "crypto-aes-256-gcm-no-padding"
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
  }
}
