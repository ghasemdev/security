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

object KeyStoreHelper {
  @RequiresApi(Build.VERSION_CODES.M)
  private const val SIGNATURE_PURPOSE = KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY

  private val keyStore by lazy {
    KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun getJwsKey(): KeyPair = getKeyPair("rsa") ?: generateKeyPair("rsa")

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
    .getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
    .apply {
      initialize(
        KeyGenParameterSpec
          .Builder(alias, SIGNATURE_PURPOSE)
          .setKeySize(2048)
          .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
          .setDigests(KeyProperties.DIGEST_SHA256)
          .setUserAuthenticationRequired(false)
          .build()
      )
    }
    .generateKeyPair()
}