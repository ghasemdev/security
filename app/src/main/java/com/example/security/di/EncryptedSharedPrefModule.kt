package com.example.security.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptedSharedPrefModule {
  private const val PREF_NAME = "secure-shared-pref"

  @Singleton
  @Provides
  fun provideSharedPref(
    @ApplicationContext context: Context
  ): SharedPreferences {
    return try {
      val mainKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .setRequestStrongBoxBacked(true)
        .build()

      EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        mainKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
      )
    } catch (_: Exception) {
      context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
  }
}
