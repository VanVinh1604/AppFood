package com.example.foodapp.Domain

data class VouchersModel(
    val voucherId: String? = null,
    val code: String? = null,
    val description: String? = null,
    val discountPercent: Double = 0.0,
    val maxDiscount: Double = 0.0,
    val minOrderValue: Double = 0.0,
    val expiresAt: String? = null,
    val usageLimit: Int = 0,
    val active: Boolean = false
)
