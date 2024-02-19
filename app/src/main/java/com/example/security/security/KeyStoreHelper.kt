package com.example.security.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec

object KeyStoreHelper {
  @RequiresApi(Build.VERSION_CODES.M)
  private const val SIGNATURE_PURPOSE = KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY

  private val keyStore by lazy {
    KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun getJwsKey(): KeyPair = getKeyPair("ec") ?: generateKeyPair("ec")

  @Synchronized
  private fun getKeyPair(alias: String): KeyPair? {
    val privateKey: PrivateKey? = keyStore?.getKey(alias, null) as? PrivateKey
    val publicKey: PublicKey? = keyStore?.getCertificate(alias)?.publicKey

    if (privateKey != null && publicKey != null) {
      return KeyPair(publicKey, privateKey)
    }
    return null
  }

  @RequiresApi(Build.VERSION_CODES.M)
  @Synchronized
  private fun generateKeyPair(alias: String): KeyPair = KeyPairGenerator
    .getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
    .apply {
      initialize(
        KeyGenParameterSpec
          .Builder(alias, SIGNATURE_PURPOSE)
          .setKeySize(256)
          .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
          .setDigests(KeyProperties.DIGEST_SHA256)
          .setUserAuthenticationRequired(false)
          .build()
      )
    }
    .generateKeyPair()
}
