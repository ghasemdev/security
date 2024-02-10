package com.example.security.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.PURPOSE_SIGN
import android.security.keystore.KeyProperties.PURPOSE_VERIFY
import android.util.Log
import androidx.annotation.RequiresApi
import okio.ByteString.Companion.toByteString
import com.example.security.json.builder.buildJsonObject
import com.example.security.json.builder.put
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.ECGenParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class JWSAuthenticator {
  private val keyStore by lazy {
    KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun getJwsKey(): KeyPair = getKeyPair("ec") ?: generateKeyPair("ec")

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
    jwk: JWK,
    header: Map<String, String>,
    payload: String,
    attachPublicKey: Boolean
  ): JWSObject {
    val jwsHeader = JWSHeader(JWSAlgorithm.ES256) {
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

  private fun computeThumbprint(publicExponent: String, modulus: String, alg: String): String {
    val requiredParams = buildJsonObject {
      put("e", publicExponent) // RSA_EXPONENT
      put("kty", alg) // KEY_TYPE
      put("n", modulus) // RSA_MODULUS
    }.toString()

    return requiredParams
      .encodeToByteArray()
      .toByteString()
      .sha256()
      .base64Url()
      .removeBase64UrlPadding()
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun jwsTest() {
    // Generate an RSA key pair
    val rsaKeyPair = getJwsKey()
    val rsaKey = ECKey.Builder(Curve.P_256, rsaKeyPair.public as ECPublicKey)
      .privateKey(rsaKeyPair.private)
      .keyIDFromThumbprint()
      .build()
    val rsaPublicKey = rsaKey.toECPublicKey()

    // Create RSA-signer with the private key
    val signer = ECDSASigner(rsaKey)

    // Prepare JWS object with simple string as payload
    var jwsObject = createJWSObject(
      jwk = rsaKey.toPublicJWK(),
      header = mapOf(
        JWS_HEADER_ACTION to "GET",
        JWS_HEADER_METHOD to "getStepOrder",
        JWS_HEADER_TIMESTAMP to "2/9/2024"
      ),
      payload = "In RSA we trust!",
      attachPublicKey = true
    )

    // Compute the RSA signature
    jwsObject.sign(signer)

    // To serialize to compact form, produces something
    val jwsString = jwsObject.serialize()
    Log.d("aaa", "jwe: $jwsString")

    // To parse the JWS and verify it, e.g. on client-side
    jwsObject = JWSObject.parse(jwsString)
    val verifier = ECDSAVerifier(rsaPublicKey)

    jwsObject.verify(verifier)

    Log.d("aaa", "verify: ${jwsObject.verify(verifier)}")
    Log.d("aaa", "signature: ${jwsObject.signature}")
    Log.d("aaa", "payload: ${jwsObject.payload}")
    Log.d("aaa", "header: ${jwsObject.header}")
//
//    val header = buildSortedJsonObject {
//      put("alg", "RS256")
//      put("typ", "JWT")
//
//      put("ra-timestamp", "2/9/2024")
//      put("ra-method", "getStepOrder")
//      put("ra-action", "GET")
//
//      put(
//        "kid", computeThumbprint(
//          publicExponent = rsaPublicKey.base64UrlPublicExponent,
//          modulus = rsaPublicKey.base64UrlModulus,
//          alg = rsaPublicKey.algorithm
//        )
//      )
//
//      putJsonObject("jwk") {
//        put("kty", rsaPublicKey.algorithm)
//        put("e", rsaPublicKey.base64UrlPublicExponent)
//        put(
//          "kid",
//          computeThumbprint(
//            publicExponent = rsaPublicKey.base64UrlPublicExponent,
//            modulus = rsaPublicKey.base64UrlModulus,
//            alg = rsaPublicKey.algorithm
//          )
//        )
//        put("n", rsaPublicKey.base64UrlModulus)
//      }
//    }.toString()
//    val base64Header = Base64.UrlSafe.encode(header.encodeToByteArray())
//    val originalHeader = Base64.UrlSafe.decode(base64Header).decodeToString()
//    Log.d("aaa", "custom: $originalHeader $base64Header")
//
//    val payload = "In RSA we trust!".trim()
//    val base64Payload = Base64.UrlSafe.encode(payload.encodeToByteArray())
//    val originalPayload = Base64.UrlSafe.decode(base64Payload).decodeToString()
//    Log.d("aaa", "custom: $originalPayload $base64Payload")
//
//    val signingData =
//      "${base64Header.removeBase64UrlPadding()}.${base64Payload.removeBase64UrlPadding()}"
//    val sign = Signature.getInstance("SHA256withRSA") // SHA256withECDSA
//    sign.initSign(rsaKeyPair.private)
//    sign.update(signingData.encodeToByteArray())
//    val signedData = Base64.UrlSafe.encode(sign.sign()).removeBase64UrlPadding()
//    val jws = "$signingData.$signedData"
//    Log.d("aaa", "custom jws: $jws")
//
//    jwsObject = JWSObject.parse(jws)
//    val verifier2 = RSASSAVerifier(rsaPublicKey)
//
//    jwsObject.verify(verifier2)
//
//    Log.d("aaa", "verify: ${jwsObject.verify(verifier2)}")
//    Log.d("aaa", "signature: ${jwsObject.signature}")
//    Log.d("aaa", "payload: ${jwsObject.payload}")
//    Log.d("aaa", "header: ${jwsObject.header}")
  }

  companion object {
    @RequiresApi(Build.VERSION_CODES.M)
    private const val SIGNATURE_PURPOSE = PURPOSE_SIGN or PURPOSE_VERIFY

    private const val JWS_HEADER_TIMESTAMP = "ra-timestamp"
    private const val JWS_HEADER_ACTION = "ra-action"
    private const val JWS_HEADER_METHOD = "ra-method"
  }
}

@ExperimentalEncodingApi
private val RSAPublicKey.base64UrlPublicExponent: String
  get() = Base64.UrlSafe
    .encode(publicExponent.toBytesUnsigned())
    .removeBase64UrlPadding()

@ExperimentalEncodingApi
private val RSAPublicKey.base64UrlModulus: String
  get() = Base64.UrlSafe
    .encode(modulus.toBytesUnsigned())
    .removeBase64UrlPadding()

private fun String.removeBase64UrlPadding() = replace("=", "")

/**
 * Returns a byte array representation of the specified big integer
 * without the sign bit.
 *
 * @return A byte array representation of the big integer, without the
 * sign bit.
 */
private fun BigInteger.toBytesUnsigned(): ByteArray {
  // Copied from Apache Commons Codec 1.8
  var bitlen = bitLength()

  // round bitlen
  bitlen = bitlen + 7 shr 3 shl 3
  val bigBytes = toByteArray()
  if (bitLength() % 8 != 0 && bitLength() / 8 + 1 == bitlen / 8) {
    return bigBytes
  }

  // set up params for copying everything but sign bit
  var startSrc = 0
  var len = bigBytes.size

  // if bigInt is exactly byte-aligned, just skip signbit in copy
  if (bitLength() % 8 == 0) {
    startSrc = 1
    len--
  }
  val startDst = bitlen / 8 - len // to pad w/ nulls as per spec
  val resizedBytes = ByteArray(bitlen / 8)
  System.arraycopy(bigBytes, startSrc, resizedBytes, startDst, len)
  return resizedBytes
}
