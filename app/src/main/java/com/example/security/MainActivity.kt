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
import com.example.security.factorial.JavaFactorial
import com.example.security.factorial.KotlinFactorial
import com.example.security.ui.theme.AppTheme
import kotlin.system.measureNanoTime

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // including IME animations, and go edge-to-edge
    // This also sets up the initial system bar style based on the platform theme
    enableEdgeToEdge()

    var result: String = ""
    var nano = measureNanoTime {
      repeat(1_000) {
        result = JavaFactorial.calculate(10)
      }
    }
    Log.d("aaa", "result: $result")
    Log.d("aaa", "time in nano: ${nano / 1_000}")

    nano = measureNanoTime {
      repeat(1_000) {
        result = KotlinFactorial.calculate(10)
      }
    }
    Log.d("aaa", "result: $result")
    Log.d("aaa", "time in nano: ${nano / 1_000}")

    nano = measureNanoTime {
      repeat(1_000) {
        result = factorial(10)
      }
    }
    Log.d("aaa", "result: $result")
    Log.d("aaa", "time in nano: ${nano / 1_000}")

    setContent {
      val darkTheme = isSystemInDarkTheme()

      AppTheme(darkTheme = darkTheme) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(text = stringFromJNI(), style = MaterialTheme.typography.h3)
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
  private external fun factorial(n: Int): String

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
