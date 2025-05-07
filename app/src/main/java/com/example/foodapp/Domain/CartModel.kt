package com.example.foodapp.Domain

data class CartModel(
    var drinkName: String? = null,
    var drinkPrice: String? = null,
    var drinkDescription: String? = null,
    var drinkImage: String? = null,
    var drinkExtra: String? = null,
    var drinkQuantity: Int? = null,
    var category: String? = null,
    val drinkDiscount: String? = null
)
