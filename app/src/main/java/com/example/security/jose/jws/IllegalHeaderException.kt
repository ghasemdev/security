package com.example.security.jose.jws

/**
 * Illegal JOSE header exception.
 */
internal class IllegalHeaderException
/**
 * Creates a new illegal JOSE header exception.
 *
 * @param message The message, `null` if not specified.
 */
  (message: String?) : Exception(message)
