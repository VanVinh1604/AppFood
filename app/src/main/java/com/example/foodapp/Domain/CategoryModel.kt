package com.example.foodapp.Domain

data class CategoryModel(
    var categoryId: String?=null,
    var category_Name: String? = null
) {
    constructor() : this(categoryId = null, category_Name = null)
}
