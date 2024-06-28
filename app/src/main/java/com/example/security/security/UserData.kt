package com.example.security.security

import androidx.annotation.Keep

@Keep
data class UserData(
  val name: String,
  val balance: Double
)
