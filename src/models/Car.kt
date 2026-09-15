package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Car(
    val id: Int? = null,
    val brand: String,
    val model: String,
    val year: Int,
    val trim: String? = null
)