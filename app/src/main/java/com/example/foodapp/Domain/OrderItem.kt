package com.example.foodapp.Domain

data class OrderItem(
    val drinkId: String? = null,
    val drinkName: String? = null,
    val drinkImage: String? = null,
    val drinkPrice: Double? = null,
    val drinkQuantity: Int? = null,  // Đổi từ quantity sang drinkQuantity
    val drinkSize: String? = null,
    var isReviewed: Boolean = false,  // Thêm var để có thể thay đổi
    var orderTime: Long = 0L  // Thêm orderTime
)