package com.example.foodapp.Domain

data class CartModel(
    var drinkQuantity: Int? = null,
    var note: String? = null,
    var listItem: List<ItemsModel>?=null,
    var deliveryDate: Long? = null,
    var orderDate: Long? = null,
    val drinkDiscount: String? = null
)
