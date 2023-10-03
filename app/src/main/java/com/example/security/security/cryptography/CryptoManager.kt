package com.example.security.security.cryptography

interface CryptoManager {
  fun encrypt(plaintext: ByteArray):  Triple<ByteArray, Int, ByteArray>
  fun decrypt(ciphertext: ByteArray, tagLength: Int, iv: ByteArray): ByteArray
}
