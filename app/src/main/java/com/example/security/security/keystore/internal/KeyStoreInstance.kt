package com.example.security.security.keystore.internal

import java.security.KeyStore

internal val keystore: KeyStore by lazy {
  KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
    load(null)
  }
}

private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
