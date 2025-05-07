package com.example.foodapp.Domain

data class CustomerModel(
    var nameCustomer: String? = null,
    var emailCustomer: String? = null,
    var passwordCustomer: String? = null,
    val profileImage: String? = null,
    var phoneNumberCustomer: String? = null,
    var addressCustomer: String? = null
)
