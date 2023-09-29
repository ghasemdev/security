package com.example.security.security.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun Context.hasStrongBox(): Boolean {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
  } else {
    false
  }
}
