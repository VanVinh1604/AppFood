package com.example.foodapp.Domain

import java.io.Serializable

data class OrderDetails(
    var customerId: String? = null,
    var customerName: String? = null,
    var drinkNames: MutableList<String>? = null,
    var drinkImages: List<String>? = null,
    var drinkPrices: MutableList<String>? = null,
    var drinkQuantities: MutableList<Int>? = null,
    var drinkSizes: MutableList<String>? = null,
    var address: String? = null,
    var totalPrice: String? = null,
    var note: String? = null,
    var phoneNumber: String? = null,
    var orderAccepted: Boolean = false,
    var paymentReceived: Boolean = false,
    var paymentStatus: String? = null,
    var deliveryStatus: String? = null,
    var itemPushKey: String? = null,
    val drinkIds: ArrayList<String>? = null, // Thêm field này


    var currentTime: Long = 0,

    var voucherCode: String? = null,
    var discountAmount: String? = null

): Serializable
