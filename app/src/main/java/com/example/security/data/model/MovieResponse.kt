package com.example.security.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieResponse(
    val `data`: List<Movie>,
    val metadata: Metadata
)
