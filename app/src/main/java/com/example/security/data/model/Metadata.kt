package com.example.security.data.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
//@Keep
data class Metadata(
    val current_page: String,
    val page_count: Int,
    val per_page: Int,
    val total_count: Int
)
