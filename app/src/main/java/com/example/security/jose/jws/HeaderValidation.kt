package com.example.security.jose.jws

import com.nimbusds.jose.UnprotectedHeader

/**
 * JOSE header validation utility.
 */
internal object HeaderValidation {
  /**
   * Ensures the parameter names in the JOSE protected header and the
   * unprotected header are disjoint.
   *
   *
   * See https://datatracker.ietf.org/doc/html/rfc7515#section-7.2.1
   *
   * @param header            The protected header, `null` if
   * not specified.
   * @param unprotectedHeader The unprotected header, `null` if
   * not specified.
   *
   * @throws IllegalHeaderException If both headers are specified and not
   * disjoint.
   */
  @Throws(IllegalHeaderException::class)
  fun ensureDisjoint(
    header: Header,
    unprotectedHeader: UnprotectedHeader
  ) {
    for (unprotectedParamName in unprotectedHeader.includedParams) {
      if (header.includedParams.contains(unprotectedParamName)) {
        throw IllegalHeaderException("The parameters in the protected header and the unprotected header must be disjoint")
      }
    }
  }
}

