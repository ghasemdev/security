package com.example.security.data.datastore

import android.content.Context
import androidx.datastore.dataStore
import com.example.security.data.datastore.serializer.SettingsSerializer
import com.example.security.data.model.Settings
import com.example.security.security.cryptography.CryptoManager
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class SettingsStorage(
  private val context: Context,
  cryptoManager: CryptoManager
) {
  private val Context.dataStore by dataStore(
    fileName = "settings",
    serializer = SettingsSerializer(cryptoManager)
  )

  val settings: Flow<Settings> = context.dataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(Settings())
      } else {
        throw exception
      }
    }

  suspend fun setSettings(settings: Settings) {
    context.dataStore.updateData { settings }
  }

  suspend fun setLanguage(language: String) {
    context.dataStore.updateData { it.copy(language = language) }
  }

  suspend fun setIsLogin(isLogin: Boolean) {
    context.dataStore.updateData { it.copy(isLogin = isLogin) }
  }

  suspend fun setUsername(username: String) {
    context.dataStore.updateData { it.copy(username = username) }
  }
}
