package com.example.foodapp.Domain

data class CommentModel(
    var productID: String? = null,
    var comment: String = "",
    var createdAt: Long = 0,
    var customerID: String = "",
    var star: Float = 0f,
    var title: String = ""
)
