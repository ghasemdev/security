package com.example.security

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import com.example.security.data.model.MovieResponse
import com.example.security.ui.theme.AppTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
  private val client = HttpClient(OkHttp) {
    install(ContentNegotiation) {
      json(
        Json {
          prettyPrint = true
          ignoreUnknownKeys = true
        }
      )
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // including IME animations, and go edge-to-edge
    // This also sets up the initial system bar style based on the platform theme
    enableEdgeToEdge()

    lifecycleScope.launch {
      val response: MovieResponse = client.get("https://moviesapi.ir/api/v1/movies").body()
      Log.d("aaa", "onCreate: $response")
    }

    setContent {
      val darkTheme = isSystemInDarkTheme()

      AppTheme(darkTheme = darkTheme) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "",
            style = MaterialTheme.typography.h3
          )
        }
      }

      // Update the edge to edge configuration to match the theme
      // This is the same parameters as the default enableEdgeToEdge call, but we manually
      // resolve whether to show a dark theme using uiState, since it can be different
      // from the configuration's dark theme value based on the user preference.
      DisposableEffect(darkTheme) {
        enableEdgeToEdge(
          statusBarStyle = SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
          ) { darkTheme },
          navigationBarStyle = SystemBarStyle.auto(
            lightScrim,
            darkScrim,
          ) { darkTheme },
        )
        onDispose {}
      }
    }
  }

  /**
   * A native method that is implemented by the 'anative' native library,
   * which is packaged with this application.
   */
  private external fun stringFromJNI(): String

  companion object {
    /**
     * The default light scrim, as defined by androidx and the platform:
     * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
     */
    private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

    /**
     * The default dark scrim, as defined by androidx and the platform:
     * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
     */
    private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

    init {
      System.loadLibrary("security")
    }
  }
}
