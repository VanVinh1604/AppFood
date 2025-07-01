package com.example.foodapp.Domain

import java.io.Serializable

data class FavoritesModel(
    val drinkId: String = "",
    val drinkName: String = "",
    val drinkImage: String = "",
    val drinkPrice: Double = 0.0,
    val drinkDescription: String? = null,
    val drinkExtra: String? = null
) : Serializable
