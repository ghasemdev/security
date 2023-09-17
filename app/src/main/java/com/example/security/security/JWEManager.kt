package com.example.security.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import android.util.Log
import androidx.annotation.RequiresApi
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSADecrypter
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.jwk.RSAKey
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.interfaces.RSAPublicKey
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class JWEManager(
  private val context: Context
) {
  private val keyStore by lazy {
    KeyStore.getInstance("AndroidKeyStore").apply {
      load(null)
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun getAesKey(encoder: EncryptionMethod): SecretKey {
    return KeyGenerator
      .getInstance(KeyProperties.KEY_ALGORITHM_AES)
      .apply {
        init(encoder.cekBitLength())
      }
      .generateKey()
  }

  private fun getKeyPair(alias: String): KeyPair? {
    val key: PrivateKey? = keyStore.getKey(alias, null) as? PrivateKey
    val cert: Certificate? = keyStore.getCertificate(alias)
    if (key != null && cert != null) {
      return KeyPair(cert.publicKey, key)
    }
    return null
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun getRSAKey(): KeyPair {
    return getKeyPair("rsa-oaep") ?: run {
      KeyPairGenerator
        .getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        .apply {
          initialize(
            KeyGenParameterSpec
              .Builder("rsa-oaep", ENCRYPTION_PURPOSE)
              .setKeySize(2048)
              .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
              .setDigests(KeyProperties.DIGEST_SHA256)
              .setUserAuthenticationRequired(false)
              .build()
          )
        }
        .generateKeyPair()
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun jweTest() {
    // The JWE alg and enc
    val alg = JWEAlgorithm.RSA_OAEP_256
    val enc = EncryptionMethod.A256GCM

    // Generate an RSA key pair
    val rsaKeyPair = getRSAKey()
    val rsaKey = RSAKey
      .Builder(rsaKeyPair.public as RSAPublicKey)
      .privateKey(rsaKeyPair.private)
      .build()
    val rsaPublicKey = rsaKey.toRSAPublicKey()
    val rsaPrivateKey = rsaKeyPair.private

    // Generate the Content Encryption Key (CEK)
    val cek = getAesKey(enc)
    Log.d("aaa", "cek: ${cek.encoded.toList()}")

    // Encrypt the JWE with the RSA public key + specified AES CEK
    var jwe = JWEObject(
      JWEHeader(alg, enc),
      Payload("Hello, world!")
    )
    jwe.encrypt(RSAEncrypter(rsaPublicKey, cek))
    val jweString = jwe.serialize()
    Log.d("aaa", "jwe: $jweString")

    // Decrypt the JWE with the RSA private key
    jwe = JWEObject.parse(jweString)
    jwe.decrypt(RSADecrypter(rsaPrivateKey))
    Log.d("aaa", "payload: ${jwe.payload}")
    Log.d("aaa", "header: ${jwe.header}")
    Log.d("aaa", "iv: ${jwe.iv}")
    Log.d("aaa", "tag: ${jwe.authTag}")
    Log.d("aaa", "key: ${jwe.encryptedKey}")
    Log.d("aaa", "cipherText: ${jwe.cipherText}")

    // Decrypt JWE with CEK directly, with the DirectDecrypter in promiscuous mode
//    jwe = JWEObject.parse(jweString)
//    jwe.decrypt(DirectDecrypter(cek, true))
  }

  companion object {
    @RequiresApi(Build.VERSION_CODES.M)
    private const val ENCRYPTION_PURPOSE = PURPOSE_ENCRYPT or PURPOSE_DECRYPT
  }
}
