package com.example.security.security.cryptography

import com.example.security.security.cryptography.model.EncryptionData
import java.io.InputStream
import java.io.OutputStream

interface CryptoManager {
  fun encrypt(plaintext: ByteArray): EncryptionData
  fun decrypt(encryptionData: EncryptionData): ByteArray

  fun encrypt(plaintext: ByteArray, outputStream: OutputStream): EncryptionData
  fun decrypt(inputStream: InputStream): ByteArray
}
