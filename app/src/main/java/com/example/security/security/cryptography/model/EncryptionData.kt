package com.example.security.security.cryptography.model

data class EncryptionData(
  val cipherText: ByteArray,
  val tagLength: Int,
  val iv: ByteArray
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as EncryptionData

    if (!cipherText.contentEquals(other.cipherText)) return false
    if (tagLength != other.tagLength) return false
    if (!iv.contentEquals(other.iv)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = cipherText.contentHashCode()
    result = 31 * result + tagLength
    result = 31 * result + iv.contentHashCode()
    return result
  }
}
