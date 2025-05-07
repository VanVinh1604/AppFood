package com.example.foodapp.Domain

data class OrderDetails(
    var customerId: String? = null,
    var customerName: String? = null,
    var drinkNames: MutableList<String>? = null,
    var drinkImage: MutableList<String>? = null,
    var drinkPrices: MutableList<String>? = null,
    var drinkQuantities: MutableList<Int>? = null,
    var address: String? = null,
    var totalPrice: String? = null,
    var note: String? = null,
    var phoneNumber: String? = null,
    var orderAccepted: Boolean = false,
    var paymentReceived: Boolean = false,
    var paymentStatus: String? = null,
    var deliveryStatus: String? = null,
    var itemPushKey: String? = null,
    var currentTime: Long = 0

)
