package com.example.security.jose.jws

import com.nimbusds.jose.HeaderParameterNames
import com.nimbusds.jose.util.JSONObjectUtils
import java.text.ParseException
import java.util.*

/**
 * JSON Web Signature (JWS) or JSON Web Encryption (JWE) unprotected header
 * (in a JSON serialisation). This class is immutable.
 */
class UnprotectedHeader private constructor(params: Map<String, Any?>) {
  /** The header parameters. */
  private val params: Map<String, Any?>

  /**
   * Creates a new unprotected header.
   *
   * @param params The header parameters. Must not be `null`.
   */
  init {
    Objects.requireNonNull(params)
    this.params = params
  }

  val keyID: String?
    /**
     * Gets the key ID (`kid`) parameter.
     *
     * @return The key ID parameter, `null` if not specified.
     */
    get() = params[HeaderParameterNames.KEY_ID] as? String?

  /**
   * Gets a parameter.
   *
   * @param name The name of the parameter. Must not be `null`.
   *
   * @return The parameter, `null` if not specified.
   */
  fun getParam(name: String): Any? {
    return params[name]
  }

  val includedParams: Set<String>
    /**
     * Gets the names of the included parameters in this unprotected
     * header.
     *
     * @return The included parameters.
     */
    get() = params.keys

  /**
   * Returns a JSON object representation of this unprotected header.
   *
   * @return The JSON object, empty if no parameters are specified.
   */
  fun toJSONObject(): Map<String, Any?> {
    val o = JSONObjectUtils.newJSONObject()
    o.putAll(params)
    return o
  }

  /**
   * Builder for constructing an unprotected JWS or JWE header.
   */
  class Builder
  /**
   * Creates a new unprotected header builder.
   */
  {
    private val params = JSONObjectUtils.newJSONObject()

    /**
     * Sets the key ID (`kid`) parameter.
     *
     * @param kid The key ID parameter, `null` if not
     * specified.
     *
     * @return This builder.
     */
    fun keyID(kid: String?): Builder {
      params[HeaderParameterNames.KEY_ID] = kid
      return this
    }

    /**
     * Sets a parameter.
     *
     * @param name  The name of the parameter. Must  not be
     * `null`.
     * @param value The value of the parameter, should map to a
     * valid JSON entity, `null` if not
     * specified.
     *
     * @return This builder.
     */
    fun param(name: String, value: Any?): Builder {
      params[name] = value
      return this
    }

    /**
     * Builds a new unprotected header.
     *
     * @return The unprotected header.
     */
    fun build(): UnprotectedHeader {
      return UnprotectedHeader(params)
    }
  }

  companion object {
    /**
     * Parses an unprotected header from the specified JSON object.
     *
     * @param jsonObject The JSON object, `null` if not specified.
     *
     * @return The unprotected header or `null`.
     *
     * @throws ParseException If the JSON object couldn't be parsed to a
     * valid unprotected header.
     */
    @Throws(ParseException::class)
    fun parse(jsonObject: Map<String, Any?>?): UnprotectedHeader? {
      if (jsonObject == null) {
        return null
      }
      var header = Builder()
      for (name in jsonObject.keys) {
        header = header.param(name, jsonObject[name])
      }
      return header.build()
    }
  }
}
