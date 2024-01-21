package com.example.security.jose.jws

/**
 * Creates a new illegal JOSE header exception.
 *
 * @param message The message, `null` if not specified.
 */
internal class IllegalHeaderException(message: String) : Exception(message)
