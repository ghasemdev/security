package com.example.security.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.PURPOSE_SIGN
import android.security.keystore.KeyProperties.PURPOSE_VERIFY
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.security.json.jsonStringify
import com.example.security.json.sortedEntries
import com.example.security.json.to
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.Certificate
import java.security.interfaces.RSAPublicKey
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
    val rsaKeyPair = getRSAKey()
    val rsaKey = RSAKey
      .Builder(rsaKeyPair.public as RSAPublicKey)
      .privateKey(rsaKeyPair.private)
      .build()
    val rsaPublicKey = rsaKey.toRSAPublicKey()
    val rsaPrivateKey = rsaKeyPair.private

    // RSA signatures require a public and private RSA key pair,
    // the public key must be made known to the JWS recipient to
    // allow the signatures to be verified.
//    val rsaJWK = RSAKeyGenerator(2048)
//      .keyID("123")
//      .generate()
//    val rsaPublicJWK = rsaJWK.toPublicJWK()

    // Create RSA-signer with the private key
    val signer = RSASSASigner(rsaPrivateKey)

    // Prepare JWS object with simple string as payload
    var jwsObject = JWSObject(
      JWSHeader.Builder(JWSAlgorithm.RS256)
        .keyID(rsaKey.keyID)
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
    val verifier = RSASSAVerifier(rsaPublicKey)

    jwsObject.verify(verifier)

    Log.d("aaa", "verify: ${jwsObject.verify(verifier)}")
    Log.d("aaa", "signature: ${jwsObject.signature}")
    Log.d("aaa", "payload: ${jwsObject.payload}")
    Log.d("aaa", "header: ${jwsObject.header}")

    val header = sortedEntries("alg" to "RS256").jsonStringify()
    val base64Header = Base64.UrlSafe.encode(header.encodeToByteArray())
    val originalHeader = Base64.UrlSafe.decode(base64Header).decodeToString()
    Log.d("aaa", "custom: $originalHeader $base64Header")

    val payload = "In RSA we trust!".trim()
    val base64Payload = Base64.UrlSafe.encode(payload.encodeToByteArray())
    val originalPayload = Base64.UrlSafe.decode(base64Payload).decodeToString()
    Log.d("aaa", "custom: $originalPayload $base64Payload")

    val signingData = "${base64Header.replace("=", "")}.${base64Payload.replace("=", "")}"
    val sign = Signature.getInstance("SHA256withRSA")
    sign.initSign(rsaKeyPair.private)
    sign.update(signingData.encodeToByteArray())
    val signedData = Base64.UrlSafe.encode(sign.sign()).replace("=", "")
    val jws = "$signingData.$signedData"
    Log.d("aaa", "custom jws: $jws")
  }

  companion object {
    @RequiresApi(Build.VERSION_CODES.M)
    private const val SIGNATURE_PURPOSE = PURPOSE_SIGN or PURPOSE_VERIFY
  }
}
