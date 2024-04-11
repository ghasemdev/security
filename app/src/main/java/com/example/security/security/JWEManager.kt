package com.example.security.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import android.security.keystore.KeyProperties.PURPOSE_SIGN
import android.security.keystore.KeyProperties.PURPOSE_VERIFY
import android.util.Log
import androidx.annotation.RequiresApi
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSADecrypter
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.KeyUse
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
  fun getRSAKey(alias: String, purpose: Int): KeyPair {
    return getKeyPair(alias) ?: run {
      KeyPairGenerator
        .getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        .apply {
          initialize(
            KeyGenParameterSpec
              .Builder(alias, purpose)
              .setKeySize(2048)
              .apply {
                if (purpose == SIGNING_PURPOSE) {
                  setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                } else if (purpose == ENCRYPTION_PURPOSE) {
                  setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                  setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                }
              }
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
    val rsaJWS = getRSAKey("rsa-jws", SIGNING_PURPOSE)
    val senderJWK = RSAKey
      .Builder(rsaJWS.public as RSAPublicKey)
      .privateKey(rsaJWS.private)
      .keyUse(KeyUse.SIGNATURE)
      .build()
    val senderPublicJWK = senderJWK.toRSAPublicKey()
    val senderPrivateJWK = rsaJWS.private

    val rsaJWE = getRSAKey("rsa-jwe", ENCRYPTION_PURPOSE)
    val recipientJWK = RSAKey
      .Builder(rsaJWE.public as RSAPublicKey)
      .privateKey(rsaJWE.private)
      .keyUse(KeyUse.ENCRYPTION)
      .build()
    val recipientPublicJWK = recipientJWK.toRSAPublicKey()
    val recipientPrivateJWK = rsaJWE.private

    // Generate the Content Encryption Key (CEK)
    val cek = getAesKey(EncryptionMethod.A256GCM)

    var jws = JWSObject(
      JWSHeader
        .Builder(JWSAlgorithm.RS256)
        .jwk(senderJWK.toPublicJWK())
        .keyID(senderJWK.keyID)
        .build(),
      Payload("Hello, world!")
    )
    jws.sign(RSASSASigner(senderPrivateJWK))

    // Encrypt the JWE with the RSA public key + specified AES CEK
    var jwe = JWEObject(
      JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM),
      Payload(jws.serialize(false))
    )
    jwe.encrypt(RSAEncrypter(recipientPublicJWK, cek))
    val jweString = jwe.serialize()
    Log.d("aaa", "jwe: $jweString")

    // Decrypt the JWE with the RSA private key
    jwe = JWEObject.parse(jweString)
    jwe.decrypt(RSADecrypter(recipientPrivateJWK))

    Log.d("aaa", "payload: ${jwe.payload}")
    Log.d("aaa", "header: ${jwe.header}")
    Log.d("aaa", "iv: ${jwe.iv}")
    Log.d("aaa", "tag: ${jwe.authTag}")
    Log.d("aaa", "key: ${jwe.encryptedKey}")
    Log.d("aaa", "cipherText: ${jwe.cipherText}")

    jws = jwe.payload.toJWSObject()

    Log.d("aaa", "payload: ${jws.payload}")
    Log.d("aaa", "header: ${jws.header}")
    Log.d("aaa", "signature: ${jws.signature}")
    Log.d("aaa", "isValid: ${jws.verify(RSASSAVerifier(senderPublicJWK))}")
  }

  companion object {
    @RequiresApi(Build.VERSION_CODES.M)
    private const val ENCRYPTION_PURPOSE = PURPOSE_ENCRYPT or PURPOSE_DECRYPT

    @RequiresApi(Build.VERSION_CODES.M)
    private const val SIGNING_PURPOSE = PURPOSE_SIGN or PURPOSE_VERIFY
  }
}
