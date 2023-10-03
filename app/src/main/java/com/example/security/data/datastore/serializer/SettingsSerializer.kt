package com.example.security.data.datastore.serializer

import androidx.datastore.core.Serializer
import com.example.security.data.model.Settings
import com.example.security.security.cryptography.CryptoManager
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class SettingsSerializer(
  private val cryptoManager: CryptoManager
) : Serializer<Settings> {
  override val defaultValue: Settings
    get() = Settings()

  override suspend fun readFrom(input: InputStream): Settings {
    val decryptedByte = cryptoManager.decrypt(input)
    return try {
      Json.decodeFromString(
        deserializer = Settings.serializer(),
        string = decryptedByte.decodeToString()
      )
    } catch (e: SerializationException) {
      e.printStackTrace()
      defaultValue
    }
  }

  override suspend fun writeTo(t: Settings, output: OutputStream) {
    cryptoManager.encrypt(
      plaintext = Json.encodeToString(
        serializer = Settings.serializer(),
        value = t
      ).encodeToByteArray(),
      outputStream = output
    )
  }
}
