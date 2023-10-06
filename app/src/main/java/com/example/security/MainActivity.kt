@file:OptIn(ExperimentalEncodingApi::class)

package com.example.security

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.security.security.cryptography.CryptoManagerImpl
import com.example.security.security.keystore.SecretKeyManagerImpl
import com.example.security.security.utils.hasStrongBox
import com.example.security.ui.theme.AppTheme
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // including IME animations, and go edge-to-edge
    // This also sets up the initial system bar style based on the platform theme
    enableEdgeToEdge()

    val secretKeyManager = SecretKeyManagerImpl(
      isSupportStrongBox = hasStrongBox()
    )

    val cryptoManager = CryptoManagerImpl(
      keyManager = secretKeyManager
    )

    setContent {
      val darkTheme = isSystemInDarkTheme()
      val coroutineScope = rememberCoroutineScope()

      var password by remember { mutableStateOf("") }

      var plainText by remember { mutableStateOf("") }
      var cipherText by remember { mutableStateOf("") }
      var iv by remember { mutableStateOf("") }
      var tagLength by remember { mutableIntStateOf(0) }

      val mutex = Mutex()

      AppTheme(darkTheme = darkTheme) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          TextField(value = password, onValueChange = { password = it })

          Button(
            onClick = {
              coroutineScope.launch(Dispatchers.IO) {
                mutex.withLock {
                  val file = File(cacheDir, "crypto")
                  if (file.exists().not()) {
                    file.createNewFile()
                  }

                  val (mCipherText, mTagLength, mIV) = cryptoManager.encrypt(
                    plaintext = password.encodeToByteArray(),
                    outputStream = file.outputStream()
                  )

                  // using decodeToString/encodeToByteArray change the size of iv
                  cipherText = Base64.encode(mCipherText)
                  iv = Base64.encode(mIV)
                  tagLength = mTagLength
                }
              }
            }
          ) {
            Text(text = "Encrypt")
          }

          Button(
            onClick = {
              coroutineScope.launch(Dispatchers.IO) {
                mutex.withLock {
                  val file = File(cacheDir, "crypto")
                  val mPlainText = cryptoManager.decrypt(inputStream = file.inputStream())

                  // using decodeToString/encodeToByteArray change the size of iv
                  plainText = mPlainText.decodeToString()
                }
              }
            }
          ) {
            Text(text = "Decrypt")
          }

          Text(text = "plainText: $plainText")
          Text(text = "cipherText: $cipherText")
          Text(text = "iv: $iv")
          Text(text = "tagLength: $tagLength")
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
