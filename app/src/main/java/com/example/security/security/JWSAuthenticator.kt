package com.example.security.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.PURPOSE_SIGN
import android.security.keystore.KeyProperties.PURPOSE_VERIFY
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.security.json.builder.buildSortedJsonObject
import com.example.security.json.builder.put
import com.nimbusds.jose.JOSEObjectType
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
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class JWSAuthenticator {
  private val keyStore by lazy {
    KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun getJwsKey(): KeyPair = getKeyPair("rsa") ?: generateKeyPair("rsa")

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

  private inline fun JWSHeader(
    alg: JWSAlgorithm,
    builderAction: JWSHeader.Builder.() -> Unit
  ): JWSHeader {
    val builder = JWSHeader.Builder(alg)
    builder.builderAction()
    return builder.build()
  }

  private fun createJWSObject(
    jwk: RSAKey,
    header: Map<String, String>,
    payload: String,
    attachPublicKey: Boolean
  ): JWSObject {
    val jwsHeader = JWSHeader(JWSAlgorithm.RS256) {
      if (attachPublicKey) jwk(jwk)
      keyID(jwk.keyID)
      header.forEach { (key, value) ->
        customParam(key, value)
      }
      base64URLEncodePayload(true)
      type(JOSEObjectType.JWT)
    }
    val detachedPayload = Payload(payload)
    return JWSObject(jwsHeader, detachedPayload)
  }

  @OptIn(ExperimentalEncodingApi::class)
  @RequiresApi(Build.VERSION_CODES.M)
  fun jwsTest() {
    // Generate an RSA key pair
    val rsaKeyPair = getJwsKey()
    val rsaKey = RSAKey
      .Builder(rsaKeyPair.public as RSAPublicKey)
      .privateKey(rsaKeyPair.private)
      .keyIDFromThumbprint()
      .build()
    val rsaPublicKey = rsaKey.toRSAPublicKey()
    val rsaPrivateKey = rsaKeyPair.private

    // Create RSA-signer with the private key
    val signer = RSASSASigner(rsaPrivateKey)

    // Prepare JWS object with simple string as payload
    var jwsObject = createJWSObject(
      jwk = rsaKey,
      header = mapOf(
        JWS_HEADER_ACTION to "GET",
        JWS_HEADER_METHOD to "getStepOrder",
        JWS_HEADER_TIMESTAMP to "2/9/2024"
      ),
      payload = "In RSA we trust!",
      attachPublicKey = false
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

    val header = buildSortedJsonObject {
      put("alg", "RS256")
      put("typ", "JWT")
      put("ra-timestamp", "2/9/2024")
      put("ra-method", "getStepOrder")
      put("ra-action", "GET")
      put("kid", "JSJe55WVX90cTVB_5iq-VDNDI84Il3HcORovrTo-pCM")
    }.toString()
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

    jwsObject = JWSObject.parse(jws)
    val verifier2 = RSASSAVerifier(rsaPublicKey)

    jwsObject.verify(verifier2)

    Log.d("aaa", "verify: ${jwsObject.verify(verifier2)}")
    Log.d("aaa", "signature: ${jwsObject.signature}")
    Log.d("aaa", "payload: ${jwsObject.payload}")
    Log.d("aaa", "header: ${jwsObject.header}")
  }

  companion object {
    @RequiresApi(Build.VERSION_CODES.M)
    private const val SIGNATURE_PURPOSE = PURPOSE_SIGN or PURPOSE_VERIFY

    private const val JWS_HEADER_TIMESTAMP = "ra-timestamp"
    private const val JWS_HEADER_ACTION = "ra-action"
    private const val JWS_HEADER_METHOD = "ra-method"
  }
}
