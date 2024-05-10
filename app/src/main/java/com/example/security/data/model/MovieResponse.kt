package com.example.security.data.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
//@Keep
data class MovieResponse(
    val `data`: List<Movie>,
    val metadata: Metadata
)
