package com.example.security.jose.jws

/** Generic payload type transformer. Implementations should be tread-safe. */
interface PayloadTransformer<T> {
  /**
   * Transforms the specified payload into the desired type.
   *
   * @param payload The payload. Not `null`.
   *
   * @return The desired type.
   */
  fun transform(payload: Payload): T
}
