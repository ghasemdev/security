package com.example.security.security.cryptography

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CryptoFile(context: Context) {
  private val cryptFile = File(context.cacheDir, FILE_NAME)

  private val mainKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .setRequestStrongBoxBacked(true)
    .build()

  private val encryptedFile = EncryptedFile.Builder(
    context,
    cryptFile,
    mainKey,
    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
  ).build()

  suspend fun openFileInput(block: (InputStream) -> Unit) = withContext(Dispatchers.IO) {
    encryptedFile.openFileInput()
      .buffered()
      .use(block)
  }

  suspend fun openFileOutput(block: (OutputStream) -> Unit) = withContext(Dispatchers.IO) {
    // File cannot exist before using openFileOutput
    if (cryptFile.exists()) {
      cryptFile.delete()
    }

    encryptedFile.openFileOutput()
      .buffered()
      .use(block)
  }

  companion object {
    private const val FILE_NAME = "crypto.txt"
  }
}