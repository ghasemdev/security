package com.example.security.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.PURPOSE_SIGN
import android.security.keystore.KeyProperties.PURPOSE_VERIFY
import android.util.Log
import androidx.annotation.RequiresApi
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class JWSAuthenticator {
  private val keyStore by lazy {
    KeyStore.getInstance("AndroidKeyStore").apply {
      load(null)
    }
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
  private fun getRSAKey(): KeyPair {
    return getKeyPair("rsa") ?: run {
      KeyPairGenerator
        .getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        .apply {
          initialize(
            KeyGenParameterSpec
              .Builder("rsa", SIGNATURE_PURPOSE)
              .setKeySize(2048)
              .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
              .setDigests(KeyProperties.DIGEST_SHA256)
              .setUserAuthenticationRequired(false)
              .build()
          )
        }
        .generateKeyPair()
    }
  }

  @OptIn(ExperimentalEncodingApi::class)
  @RequiresApi(Build.VERSION_CODES.M)
  fun jwsTest() {
    // Generate an RSA key pair
//    val rsaKeyPair = getRSAKey()
//    val rsaKey = RSAKey
//      .Builder(rsaKeyPair.public as RSAPublicKey)
//      .privateKey(rsaKeyPair.private)
//      .build()
//    val rsaPublicKey = rsaKey.toRSAPublicKey()
//    val rsaPrivateKey = rsaKeyPair.private

    // RSA signatures require a public and private RSA key pair,
    // the public key must be made known to the JWS recipient to
    // allow the signatures to be verified.
    val rsaJWK = RSAKeyGenerator(2048)
      .keyID("123")
      .generate()
    val rsaPublicJWK = rsaJWK.toPublicJWK()

    // Create RSA-signer with the private key
    val signer = RSASSASigner(rsaJWK)

    // Prepare JWS object with simple string as payload
    var jwsObject = JWSObject(
      JWSHeader.Builder(JWSAlgorithm.RS256)
        .keyID(rsaJWK.keyID)
        .build(),
      Payload("In RSA we trust!")
    )

    // Compute the RSA signature
    jwsObject.sign(signer)

    // To serialize to compact form, produces something
    val jwsString = jwsObject.serialize()
    Log.d("aaa", "jwe: $jwsString")

    // To parse the JWS and verify it, e.g. on client-side
    jwsObject = JWSObject.parse(jwsString)
    val verifier = RSASSAVerifier(rsaPublicJWK)

    jwsObject.verify(verifier)

    Log.d("aaa", "verify: ${jwsObject.verify(verifier)}")
    Log.d("aaa", "signature: ${jwsObject.signature}")
    Log.d("aaa", "payload: ${jwsObject.payload}")
    Log.d("aaa", "header: ${jwsObject.header}")

    Log.d("aaa", "UrlSafe: ${Base64.UrlSafe.encode("a".encodeToByteArray())}")
  }

  companion object {
    @RequiresApi(Build.VERSION_CODES.M)
    private const val SIGNATURE_PURPOSE = PURPOSE_SIGN or PURPOSE_VERIFY
  }
}
