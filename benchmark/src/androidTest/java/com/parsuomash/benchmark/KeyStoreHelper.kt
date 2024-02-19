package com.parsuomash.benchmark

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object KeyStoreHelper {
  @RequiresApi(Build.VERSION_CODES.M)
  private const val SIGNATURE_PURPOSE = KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY

  private val keyStore by lazy {
    KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
  }

  @Synchronized
  fun getKeyPair(alias: String): KeyPair? {
    val privateKey: PrivateKey? = keyStore?.getKey(alias, null) as? PrivateKey
    val publicKey: PublicKey? = keyStore?.getCertificate(alias)?.publicKey

    if (privateKey != null && publicKey != null) {
      return KeyPair(publicKey, privateKey)
    }
    return null
  }

  @RequiresApi(Build.VERSION_CODES.M)
  @Synchronized
  fun generateKeyPair(alias: String): KeyPair = KeyPairGenerator
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

  @OptIn(ExperimentalEncodingApi::class)
  @Synchronized
  fun sign(data: String, privateKey: PrivateKey): String {
    val sign = Signature.getInstance("SHA256withRSA")
    sign.initSign(privateKey)
    sign.update(data.encodeToByteArray())
    return Base64.UrlSafe.encode(sign.sign())
  }
}
