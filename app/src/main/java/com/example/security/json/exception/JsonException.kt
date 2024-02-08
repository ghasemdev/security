package com.example.security.json.exception

import kotlinx.serialization.SerializationException

/**
 * Generic exception indicating a problem with JSON serialization and deserialization.
 */
internal open class JsonException(message: String) : SerializationException(message)
