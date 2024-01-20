package com.example.security.jose.jws
/**
 * The base abstract class for unsecured (`alg=none`), JSON Web Signature
 * (JWS) and JSON Web Encryption (JWE) headers.
 *
 * The header may also include {@link #getCustomParams custom
 * parameters}; these will be serialised and parsed along the registered ones.
 */
abstract class Header
