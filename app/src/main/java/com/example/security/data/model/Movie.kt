package com.example.security.data.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
//@Keep
data class Movie(
    val country: String,
    val genres: List<String>,
    val id: Int,
    val images: List<String>,
    val imdb_rating: String,
    val poster: String,
    val title: String,
    val year: String
)