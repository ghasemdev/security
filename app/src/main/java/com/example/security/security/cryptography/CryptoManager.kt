package com.example.security.security.cryptography

import com.example.security.security.cryptography.model.EncryptionData

interface CryptoManager {
  fun encrypt(plaintext: ByteArray): EncryptionData
  fun decrypt(encryptionData: EncryptionData): ByteArray
}
