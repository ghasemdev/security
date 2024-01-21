package com.example.security.jose.jws

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.PlainHeader
import com.nimbusds.jose.UnprotectedHeader
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jose.util.JSONObjectUtils
import java.text.ParseException

/**
 * The base abstract class for unsecured (`alg=none`), JSON Web Signature
 * (JWS) and JSON Web Encryption (JWE) headers.
 *
 * The header may also include [getCustomParams]; these will be serialised and parsed along the registered ones.
 */
open class Header {
  /** The algorithm (`alg`) parameter. */
  var algorithm: Algorithm
    private set

  /** The JOSE object type (`typ`) parameter. */
  var type: JOSEObjectType? = null
    private set

  /** The content type (`cty`) parameter. */
  var contentType: String? = null
    private set

  /** The critical headers (`crit`) parameter. */
  var criticalParams: Set<String>? = null
    private set

  /** Custom header parameters. */
  var customParams: Map<String, Any>
    private set

  /**
   * The original parsed Base64URL, `null` if the header was
   * created from scratch.
   */
  var parsedBase64URL: Base64URL? = null
    private set

  /**
   * Gets the names of all included parameters (registered and custom) in
   * the header instance.
   *
   * @return The included parameters.
   */
  val includedParams: Set<String>
    get() {
      val includedParameters = customParams.keys.toHashSet()
      includedParameters.add(HeaderParameterNames.ALGORITHM)

      if (type != null) {
        includedParameters.add(HeaderParameterNames.TYPE)
      }

      if (contentType != null) {
        includedParameters.add(HeaderParameterNames.CONTENT_TYPE)
      }

      if (criticalParams != null && criticalParams!!.isNotEmpty()) {
        includedParameters.add(HeaderParameterNames.CRITICAL)
      }

      return includedParameters
    }

  /**
   * Creates a new abstract header.
   *
   * @param algorithm             The algorithm (`alg`) parameter.
   * @param type             The type (`typ`) parameter,
   *                        (`null`) if not specified.
   * @param contentType             The content type (`cty`) parameter,
   *                        (`null`) if not specified.
   * @param criticalParams            The names of the critical header
   *                        (`crit`) parameters, empty set or
   *                        (`null`) if none.
   * @param customParams    The custom parameters, empty map or
   *                        (`null`) if none.
   * @param parsedBase64URL The parsed Base64URL, (`null`) if the
   *                        header is created from scratch.
   */
  protected constructor(
    algorithm: Algorithm,
    type: JOSEObjectType?,
    contentType: String?,
    criticalParams: Set<String>?,
    customParams: Map<String, Any>?,
    parsedBase64URL: Base64URL?
  ) {
    this.algorithm = algorithm
    this.type = type
    this.contentType = contentType
    this.criticalParams = criticalParams?.toMutableSet()
    this.customParams = customParams?.toMutableMap() ?: EMPTY_CUSTOM_PARAMS
    this.parsedBase64URL = parsedBase64URL
  }

  /**
   * Deep copy constructor.
   *
   * @param header The header to copy.
   */
  protected constructor(header: Header) : this(
    header.algorithm,
    header.type,
    header.contentType,
    header.criticalParams,
    header.customParams,
    header.parsedBase64URL
  )

  /**
   * Gets a custom (non-registered) parameter.
   *
   * @param name The name of the custom parameter.
   *
   * @return The custom parameter, `null` if not specified.
   */
  fun getCustomParam(name: String): Any? = customParams.getOrDefault(name, null)

  /**
   * Returns a JSON object representation of the header. All custom
   * parameters are included if they serialise to a JSON entity and
   * their names don't conflict with the registered ones.
   *
   * @return The JSON object representation of the header.
   */
  fun toJSONObject(): Map<String, Any> {
    // Include custom parameters, they will be overwritten if their
    // names match specified registered ones.
    val o = hashMapOf<String, Any>()
    o.putAll(customParams)
    o[HeaderParameterNames.ALGORITHM] = algorithm.toString()

    if (type != null) {
      o[HeaderParameterNames.TYPE] = type.toString()
    }

    if (contentType != null) {
      o[HeaderParameterNames.CONTENT_TYPE] = contentType!!
    }

    if (criticalParams != null && criticalParams!!.isNotEmpty()) {
      o[HeaderParameterNames.CRITICAL] = criticalParams!!.toMutableList()
    }

    return o
  }

  /**
   * Returns a JSON string representation of the header. All custom
   * parameters will be included if they serialise to a JSON entity, and
   * their names don't conflict with the registered ones.
   *
   * @return The JSON string representation of the header.
   */
  override fun toString(): String {
    return JSONObjectUtils.toJSONString(toJSONObject())
  }

  /**
   * Returns a Base64URL representation of the header. If the header was
   * parsed always returns the original Base64URL (required for JWS
   * validation and authenticated JWE decryption).
   *
   * @return The original parsed Base64URL representation of the header,
   * or a new Base64URL representation if the header was created
   * from scratch.
   */
  fun toBase64URL(): Base64URL? {
    return if (parsedBase64URL == null) {
      // Header was created from scratch, return new Base64URL
      Base64URL.encode(toString())
    } else {
      // Header was parsed, return original Base64URL
      parsedBase64URL
    }
  }

  /**
   * Parses an algorithm (`alg`) parameter from the specified
   * header JSON object. Intended for initial parsing of unsecured (plain), JWS and JWE headers.
   *
   * The algorithm type (none, JWS or JWE) is determined by inspecting
   * the algorithm name for "none" and the presence of an "enc" parameter.
   *
   * @param json The JSON object to parse. Must not be `null`.
   *
   * @return The algorithm, an instance of [Algorithm.NONE],
   * [JWSAlgorithm] or [JWEAlgorithm]. `null` if not found.
   *
   * @throws ParseException If the `alg` parameter couldn't be parsed.
   */
  @Throws(ParseException::class)
  fun parseAlgorithm(json: Map<String, Any>): Algorithm {
    val algName = JSONObjectUtils.getString(json, HeaderParameterNames.ALGORITHM)
      ?: throw ParseException("Missing \"alg\" in header JSON object", 0)

    // Infer algorithm type
    return if (algName == Algorithm.NONE.name) {
      // Plain
      Algorithm.NONE
    } else if (json.containsKey(HeaderParameterNames.ENCRYPTION_ALGORITHM)) {
      // JWE
      JWEAlgorithm.parse(algName)
    } else {
      // JWS
      JWSAlgorithm.parse(algName)
    }
  }

  /**
   * Parses a [PlainHeader], [JWSHeader] or [JWEHeader]
   * from the specified JSON object.
   *
   * @param jsonObject      The JSON object to parse. Must not be
   * `null`.
   *
   * @return The header.
   *
   * @throws ParseException If the specified JSON object doesn't
   * represent a valid header.
   */
  @Throws(ParseException::class)
  open fun parse(jsonObject: Map<String?, Any?>?): com.nimbusds.jose.Header? {
    return com.nimbusds.jose.Header.parse(jsonObject, null)
  }

  /**
   * Join a [PlainHeader], [JWSHeader] or [JWEHeader]
   * with an Unprotected header.
   *
   * @param unprotected     The Unprotected header. `null`
   * if not applicable.
   *
   * @return The header.
   *
   * @throws ParseException If the specified Unprotected header can not be
   * merged to protected header.
   */
  @Throws(ParseException::class)
  open fun join(unprotected: UnprotectedHeader): Header {
    val jsonObject = toJSONObject().toMutableMap()
    try {
      HeaderValidation.ensureDisjoint(this, unprotected)
    } catch (e: IllegalHeaderException) {
      throw ParseException(e.message, 0)
    }
    jsonObject.putAll(unprotected.toJSONObject())
    return Header.parse(jsonObject, null)
  }

  /**
   * Parses a [PlainHeader], [JWSHeader] or [JWEHeader]
   * from the specified JSON object.
   *
   * @param jsonObject      The JSON object to parse. Must not be
   * `null`.
   * @param parsedBase64URL The original parsed Base64URL, `null`
   * if not applicable.
   *
   * @return The header.
   *
   * @throws ParseException If the specified JSON object doesn't
   * represent a valid header.
   */
  @Throws(ParseException::class)
  fun parse(jsonObject: Map<String, Any>, parsedBase64URL: Base64URL?): Header {
//    val algName = JSONObjectUtils.getString(jsonObject, HeaderParameterNames.ALGORITHM)
//    return if (jsonObject.containsKey(HeaderParameterNames.ENCRYPTION_ALGORITHM)) {
//      // JWE
//      JWEHeader.parse(jsonObject, parsedBase64URL)
//    } else if (Algorithm.NONE.name == algName) {
//      // Plain
//      PlainHeader.parse(jsonObject, parsedBase64URL)
//    } else if (jsonObject.containsKey(HeaderParameterNames.ALGORITHM)) {
//      // JWS
//      JWSHeader.parse(jsonObject, parsedBase64URL)
//    } else {
//      throw ParseException("Missing \"alg\" in header JSON object", 0)
//    }
    return Header()
  }

  /**
   * Parses a [PlainHeader], [JWSHeader] or [JWEHeader] from the specified JSON object string.
   *
   * @param jsonString      The JSON object string to parse.
   *
   * @return The header.
   *
   * @throws ParseException If the specified JSON object string doesn't represent a valid header.
   */
  @Throws(ParseException::class)
  fun parse(jsonString: String): Header? = parse(jsonString, null)

  /**
   * Parses a [PlainHeader], [JWSHeader] or [JWEHeader] from the specified JSON object string.
   *
   * @param jsonString      The JSON object string to parse.
   * @param parsedBase64URL The original parsed Base64URL, `null` if not applicable.
   *
   * @return The header.
   *
   * @throws ParseException If the specified JSON object string doesn't represent a valid header.
   */
  @Throws(ParseException::class)
  fun parse(jsonString: String, parsedBase64URL: Base64URL?): Header? {
    val jsonObject = JSONObjectUtils.parse(jsonString, MAX_HEADER_STRING_LENGTH)
    return Header.parse(jsonObject, parsedBase64URL)
  }

  /**
   * Parses a [PlainHeader], [JWSHeader] or [JWEHeader] from the specified Base64URL.
   *
   * @param base64URL The Base64URL to parse.
   *
   * @return The header.
   *
   * @throws ParseException If the specified Base64URL doesn't represent a valid header.
   */
  @Throws(ParseException::class)
  fun parse(base64URL: Base64URL): Header? = parse(base64URL.decodeToString(), base64URL)

  companion object {
    /**
     * The max allowed string length when parsing a JOSE header (after the
     * BASE64URL decoding). 20K chars should be enough to accommodate
     * JOSE headers with an X.509 certificate chain in the `x5c`
     * header parameter.
     */
    const val MAX_HEADER_STRING_LENGTH = 20_000

    /** Empty custom parameters constant. */
    private val EMPTY_CUSTOM_PARAMS = hashMapOf<String, Any>()
  }
}
