package com.example.security

object n {
  external fun s(): String

  init {
    System.loadLibrary("security")
  }
}
