package com.example.security.security.cryptography

import com.example.security.security.cryptography.model.EncryptionData
import com.example.security.security.keystore.SecretKeyManager
import com.example.security.security.keystore.SecretKeyManager.Companion.ALGORITHM
import com.example.security.security.keystore.SecretKeyManager.Companion.BLOCK_MODE
import com.example.security.security.keystore.SecretKeyManager.Companion.PADDING
import java.io.InputStream
import java.io.OutputStream
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

  override fun encrypt(plaintext: ByteArray): EncryptionData {
    val encryptCipher = encryptCipher()
    val gcmParameter = encryptCipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
    return EncryptionData(encryptCipher.doFinal(plaintext), gcmParameter.tLen, gcmParameter.iv)
  }

  override fun encrypt(plaintext: ByteArray, outputStream: OutputStream): EncryptionData {
    val encryptionData = encrypt(plaintext)
    with(encryptionData) {
      outputStream.use {
        it.write(tagLength)
        it.write(iv.size)
        it.write(iv)
        it.write(cipherText.size)
        it.write(cipherText)
      }
    }
    return encryptionData
  }

  override fun decrypt(encryptionData: EncryptionData): ByteArray =
    decryptCipher(encryptionData.tagLength, encryptionData.iv).doFinal(encryptionData.cipherText)

  override fun decrypt(inputStream: InputStream): ByteArray = inputStream.use {
    val tagLength = it.read()

    val ivSize = it.read()
    val iv = ByteArray(ivSize)
    it.read(iv)

    val cipherTextSize = it.read()
    val cipherText = ByteArray(cipherTextSize)
    it.read(cipherText)

    decrypt(EncryptionData(cipherText, tagLength, iv))
  }

  companion object {
    private const val ALIAS_KEY = "crypto-aes-256-gcm-no-padding"
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
  }
}