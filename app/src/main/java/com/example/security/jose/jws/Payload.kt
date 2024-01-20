package com.example.security.jose.jws

import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jose.util.JSONObjectUtils
import com.nimbusds.jwt.SignedJWT
import java.text.ParseException

/**
 * Payload of an unsecured (plain), JSON Web Signature (JWS) or JSON Web
 * Encryption (JWE) object. Supports JSON object, string, byte array,
 * Base64URL, JWS object and signed JWT payload representations. This class is
 * immutable.
 *
 * UTF-8 is the character set for all conversions between strings and byte
 * arrays.
 */
class Payload {
  /** Enumeration of the original data types used to create a [Payload]. */
  enum class Origin {
    /** The payload was created from a JSON object. */
    JSON,

    /** The payload was created from a string. */
    STRING,

    /** The payload was created from a byte array. */
    BYTE_ARRAY,

    /** The payload was created from a Base64URL-encoded object. */
    BASE64URL,

    /** The payload was created from a JWS object. */
    JWS_OBJECT,

    /** The payload was created from a signed JSON Web Token (JWT). */
    SIGNED_JWT
  }

  /** The original payload data type. */
  var origin: Origin
    private set

  /** The JSON object representation. */
  private lateinit var jsonObject: Map<String, Any>

  /** The string representation. */
  private lateinit var string: String

  /** The byte array representation. */
  private lateinit var bytes: ByteArray

  /** The Base64URL representation. */
  private lateinit var base64URL: Base64URL

  /** The JWS object representation. */
  private lateinit var jwsObject: JWSObject

  /** The signed JWT representation. */
  private lateinit var signedJWT: SignedJWT

  /**
   * Creates a new payload from the specified JSON object.
   *
   * @param jsonObject The JSON object representing the payload. Must not be `null`.
   */
  constructor(jsonObject: Map<String, Any>) {
    this.jsonObject = jsonObject
    origin = Origin.JSON
  }

  /**
   * Creates a new payload from the specified string.
   *
   * @param string The string representing the payload. Must not be `null`.
   */
  constructor(string: String) {
    this.string = string
    origin = Origin.STRING
  }

  /**
   * Creates a new payload from the specified byte array.
   *
   * @param bytes The byte array representing the payload. Must not be `null`.
   */
  constructor(bytes: ByteArray) {
    this.bytes = bytes
    origin = Origin.BYTE_ARRAY
  }

  /**
   * Creates a new payload from the specified Base64URL-encoded object.
   *
   * @param base64URL The Base64URL-encoded object representing the payload. Must not be `null`.
   */
  constructor(base64URL: Base64URL) {
    this.base64URL = base64URL
    origin = Origin.BASE64URL
  }

  /**
   * Creates a new payload from the specified JWS object. Intended for
   * signed then encrypted JOSE objects.
   *
   * @param jwsObject The JWS object representing the payload.
   * Must be in a signed state and not `null`.
   */
  constructor(jwsObject: JWSObject) {
    require(jwsObject.state != JWSObject.State.UNSIGNED) { "The JWS object must be signed" }

    this.jwsObject = jwsObject
    origin = Origin.JWS_OBJECT
  }

  /**
   * Creates a new payload from the specified signed JSON Web Token
   * (JWT). Intended for signed then encrypted JWTs.
   *
   * @param signedJWT The signed JWT representing the payload.
   * Must be in a signed state and not `null`.
   */
  constructor(signedJWT: SignedJWT) {
    require(signedJWT.state != JWSObject.State.UNSIGNED) { "The JWT must be signed" }

    this.signedJWT = signedJWT
    jwsObject = signedJWT // The signed JWT is also a JWS
    origin = Origin.SIGNED_JWT
  }

  /**
   * Returns a JSON object representation of this payload.
   *
   * @return The JSON object representation,
   * `null` if the payload couldn't be converted to a JSON object.
   */
  fun toJSONObject(): Map<String, Any>? {
    if (::jsonObject.isInitialized) {
      return jsonObject
    }

    // Convert
    val string = toString()
    if (string.isNullString()) {
      // to string conversion failed
      return null
    }

    return try {
      JSONObjectUtils.parse(string)
    } catch (e: ParseException) {
      // Payload not a JSON object
      null
    }
  }

  /**
   * Returns a string representation of this payload.
   *
   * @return The string representation.
   */
  override fun toString(): String {
    if (::string.isInitialized) {
      return string
    }

    // Convert
    return if (::jwsObject.isInitialized) {
      if (jwsObject.parsedString != null) {
        jwsObject.parsedString
      } else {
        jwsObject.serialize()
      }
    } else if (::jsonObject.isInitialized) {
      JSONObjectUtils.toJSONString(jsonObject)
    } else if (::bytes.isInitialized) {
      bytes.decodeToString()
    } else if (::base64URL.isInitialized) {
      base64URL.decodeToString()
    } else {
      "null" // should never happen
    }
  }

  /**
   * Returns a byte array representation of this payload.
   *
   * @return The byte array representation.
   */
  fun toBytes(): ByteArray? {
    if (::bytes.isInitialized) {
      return bytes
    }

    // Convert
    if (::base64URL.isInitialized) {
      return base64URL.decode()
    }

    val string = toString()
    return if (string.isNullString()) null else string.encodeToByteArray()
  }

  /**
   * Returns a Base64URL representation of this payload, as required for
   * JOSE serialisation (see RFC 7515, section 7).
   *
   * @return The Base64URL representation.
   */
  fun toBase64URL(): Base64URL {
    if (::base64URL.isInitialized) {
      return base64URL
    }

    // Convert
    return Base64URL.encode(toBytes())
  }

  /**
   * Returns a JWS object representation of this payload. Intended for
   * signed then encrypted JOSE objects.
   *
   * @return The JWS object representation, `null` if the payload
   * couldn't be converted to a JWS object.
   */
  fun toJWSObject(): JWSObject? {
    if (::jwsObject.isInitialized) {
      return jwsObject
    }

    return try {
      JWSObject.parse(toString())
    } catch (e: ParseException) {
      null
    }
  }

  /**
   * Returns a signed JSON Web Token (JWT) representation of this
   * payload. Intended for signed then encrypted JWTs.
   *
   * @return The signed JWT representation, `null` if the payload
   * couldn't be converted to a signed JWT.
   */
  fun toSignedJWT(): SignedJWT? {
    if (::signedJWT.isInitialized) {
      return signedJWT
    }

    return try {
      SignedJWT.parse(toString())
    } catch (e: ParseException) {
      null
    }
  }

  /**
   * Returns a transformation of this payload.
   *
   * @param <T> Type of the result.
   * @param transformer The payload transformer. Must not be `null`.
   *
   * @return The transformed payload.
   */
  fun <T> toType(transformer: PayloadTransformer<T>): T {
    return transformer.transform(this)
  }
}

fun String.isNullString(): Boolean = this == "null"
