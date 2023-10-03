package com.example.security.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
  val username: String = "",
  val language: String = "en",
  val isLogin: Boolean = false
)
