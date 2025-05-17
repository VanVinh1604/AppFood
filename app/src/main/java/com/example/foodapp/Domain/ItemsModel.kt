package com.example.foodapp.Domain

import java.io.Serializable

data class ItemsModel(
    var drinkId: String? = null,
    val drinkName: String? = null,
    var drinkPrice: Double? = null,
    val drinkDescription: String? = null,
    var drinkImage: String? = null,
    val drinkExtra: String? = null,
    val trending: Boolean = false,
    val categoryId: String? = null,
    val bestSeller: Boolean = false,
    var discountValue: String? = null,
    var createdAt: String? = null,
    var size: String?=null,
    var numberInCart: Int = 1,
    var rating: Float?= null,
    var endAt: String? = null
):Serializable
