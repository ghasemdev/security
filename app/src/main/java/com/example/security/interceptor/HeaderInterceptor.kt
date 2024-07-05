package com.example.security.interceptor

import androidx.annotation.Keep
import okhttp3.Interceptor
import okhttp3.Response

@Keep
class HeaderInterceptor(
  private val headers: Map<String, String> = emptyMap()
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val newRequest = request.newBuilder()
      .apply {
        for ((name, value) in headers) {
          header(name, value)
        }
      }
      .build()
    return chain.proceed(newRequest)
  }
}
