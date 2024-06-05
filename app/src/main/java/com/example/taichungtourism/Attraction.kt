package com.example.taichungtourism

import java.io.Serializable

data class Attraction(
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val introduction: String,
    val imageUrl: String
): Serializable