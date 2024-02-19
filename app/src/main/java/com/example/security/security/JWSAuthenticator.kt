package com.example.security.security

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import okio.ByteString.Companion.toByteString
import com.example.security.json.builder.buildJsonObject
import com.example.security.json.builder.buildSortedJsonObject
import com.example.security.json.builder.put
import com.example.security.json.builder.putJsonObject
import com.example.security.security.KeyStoreHelper.getJwsKey
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
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class JWSAuthenticator {
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

  private fun computeThumbprint(crv: String, x: String, y: String, alg: String): String {
    val requiredParams = buildJsonObject {
      put("crv", crv) // ELLIPTIC_CURVE
      put("kty", alg) // KEY_TYPE
      put("x", x) // ELLIPTIC_CURVE_X_COORDINATE
      put("y", y) // ELLIPTIC_CURVE_Y_COORDINATE
    }.toString()

    return requiredParams
      .encodeToByteArray()
      .toByteString()
      .sha256()
      .base64Url()
      .removeBase64UrlPadding()
  }

  @OptIn(ExperimentalEncodingApi::class)
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
    Log.d("aaa", "jws: $jwsString")

    // To parse the JWS and verify it, e.g. on client-side
    jwsObject = JWSObject.parse(jwsString)
    val verifier = ECDSAVerifier(rsaPublicKey)

    jwsObject.verify(verifier)

    Log.d("aaa", "verify: ${jwsObject.verify(verifier)}")
    Log.d("aaa", "signature: ${jwsObject.signature}")
    Log.d("aaa", "payload: ${jwsObject.payload}")
    Log.d("aaa", "header: ${jwsObject.header}")

    Log.d("aaa", "rsa key base64Url: ${rsaPublicKey.base64UrlEncoded}")
    Log.d("aaa", "rsa key pem: ${rsaPublicKey.pem}")

    val header = buildSortedJsonObject {
      put("alg", "ES256")
      put("typ", "JWT")

      put("ra-timestamp", "2/9/2024")
      put("ra-method", "getStepOrder")
      put("ra-action", "GET")

      put(
        "kid", computeThumbprint(
          alg = rsaPublicKey.algorithm, // EC
          crv = Curve.forECParameterSpec(rsaPublicKey.params).toString(), // P-256
          x = rsaPublicKey.base64UrlX,
          y = rsaPublicKey.base64UrlY
        )
      )

      putJsonObject("jwk") {
        put("kty", rsaPublicKey.algorithm)
        put("crv", Curve.forECParameterSpec(rsaPublicKey.params).toString())
        put(
          "kid",
          computeThumbprint(
            alg = rsaPublicKey.algorithm, // EC
            crv = Curve.forECParameterSpec(rsaPublicKey.params).toString(), // P-256
            x = rsaPublicKey.base64UrlX,
            y = rsaPublicKey.base64UrlY
          )
        )
        put("x", rsaPublicKey.base64UrlX)
        put("y", rsaPublicKey.base64UrlY)
      }
    }.toString()
    val base64Header = Base64.UrlSafe.encode(header.encodeToByteArray())
    val originalHeader = Base64.UrlSafe.decode(base64Header).decodeToString()
    Log.d("aaa", "custom: $originalHeader $base64Header")

    val payload = "In RSA we trust!".trim()
    val base64Payload = Base64.UrlSafe.encode(payload.encodeToByteArray())
    val originalPayload = Base64.UrlSafe.decode(base64Payload).decodeToString()
    Log.d("aaa", "custom: $originalPayload $base64Payload")

    val signingData =
      "${base64Header.removeBase64UrlPadding()}.${base64Payload.removeBase64UrlPadding()}"
    val sign = Signature.getInstance("SHA256withECDSA")
    sign.initSign(rsaKeyPair.private)
    sign.update(signingData.encodeToByteArray())
    val signedData = Base64.UrlSafe
      .encode(transcodeSignatureToConcat(sign.sign(), 64))
      .removeBase64UrlPadding()
    val jws = "$signingData.$signedData"
    Log.d("aaa", "custom jws: $jws")

    jwsObject = JWSObject.parse(jws)
    val verifier2 = ECDSAVerifier(rsaPublicKey)

    jwsObject.verify(verifier2)

    Log.d("aaa", "verify: ${jwsObject.verify(verifier2)}")
    Log.d("aaa", "signature: ${jwsObject.signature}")
    Log.d("aaa", "payload: ${jwsObject.payload}")
    Log.d("aaa", "header: ${jwsObject.header}")
  }

  companion object {
    private const val JWS_HEADER_TIMESTAMP = "ra-timestamp"
    private const val JWS_HEADER_ACTION = "ra-action"
    private const val JWS_HEADER_METHOD = "ra-method"
  }
}

@ExperimentalEncodingApi
private val PublicKey.base64UrlEncoded: String
  get() = Base64.UrlSafe
    .encode(encoded)
    .removeBase64UrlPadding()

@ExperimentalEncodingApi
private val PublicKey.pem: String
  get() = buildString {
    val publicKeyContent = Base64.Default.encode(encoded).removeBase64UrlPadding()
    append("-----BEGIN PUBLIC KEY-----\n")
    for (row in publicKeyContent.chunked(64)) {
      append(row + "\n")
    }
    append("-----END PUBLIC KEY-----")
  }

@ExperimentalEncodingApi
private val ECPublicKey.base64UrlX: String
  get() = Base64.UrlSafe
    .encode(coordinate(params.curve.field.fieldSize, w.affineX))
    .removeBase64UrlPadding()

@ExperimentalEncodingApi
private val ECPublicKey.base64UrlY: String
  get() = Base64.UrlSafe
    .encode(coordinate(params.curve.field.fieldSize, w.affineY))
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

/**
 * Returns the ByteArray of the specified elliptic curve 'x',
 * 'y' or 'd' coordinate, with leading zero padding up to the specified
 * field size in bits.
 *
 * @param fieldSize  The field size in bits.
 * @param coordinate The elliptic curve coordinate.
 *
 * @return The ByteArray coordinate, with leading zero padding
 * up to the curve's field size.
 */
private fun coordinate(fieldSize: Int, coordinate: BigInteger): ByteArray {
  val notPadded = coordinate.toBytesUnsigned()
  val bytesToOutput = (fieldSize + 7) / 8
  if (notPadded.size >= bytesToOutput) {
    // Greater-than check to prevent exception on malformed
    // key below
    return notPadded
  }
  val padded = ByteArray(bytesToOutput)
  System.arraycopy(notPadded, 0, padded, bytesToOutput - notPadded.size, notPadded.size)
  return padded
}

/**
 * Transcode the JCA ASN.1/DER-encoded signature into the concatenated
 * R + S format expected by ECDSA JWS.
 *
 * @param derSignature The ASN1./DER-encoded. Must not be `null`.
 * @param outputLength The expected length of the ECDSA JWS signature.
 *
 * @return The ECDSA JWS encoded signature.
 *
 * @throws IllegalStateException If the ASN.1/DER signature format is invalid.
 */
@Throws(IllegalStateException::class)
private fun transcodeSignatureToConcat(derSignature: ByteArray, outputLength: Int): ByteArray {
  if (derSignature.size < 8 || derSignature[0].toInt() != 48) {
    error("Invalid ECDSA signature format")
  }
  val offset: Int = if (derSignature[1] > 0) {
    2
  } else if (derSignature[1] == 0x81.toByte()) {
    3
  } else {
    error("Invalid ECDSA signature format")
  }
  val rLength = derSignature[offset + 1]
  var i: Int = rLength.toInt()
  while (i > 0 && derSignature[offset + 2 + rLength - i].toInt() == 0) {
    i--
  }
  val sLength = derSignature[offset + 2 + rLength + 1]
  var j: Int = sLength.toInt()
  while (j > 0 && derSignature[offset + 2 + rLength + 2 + sLength - j].toInt() == 0) {
    j--
  }
  var rawLen = i.coerceAtLeast(j)
  rawLen = rawLen.coerceAtLeast(outputLength / 2)
  if (derSignature[offset - 1].toInt() and 0xff != derSignature.size - offset || derSignature[offset - 1].toInt() and 0xff != 2 + rLength + 2 + sLength || derSignature[offset].toInt() != 2 || derSignature[offset + 2 + rLength].toInt() != 2) {
    error("Invalid ECDSA signature format")
  }
  val concatSignature = ByteArray(2 * rawLen)
  System.arraycopy(derSignature, offset + 2 + rLength - i, concatSignature, rawLen - i, i)
  System.arraycopy(
    derSignature,
    offset + 2 + rLength + 2 + sLength - j,
    concatSignature,
    2 * rawLen - j,
    j
  )
  return concatSignature
}
