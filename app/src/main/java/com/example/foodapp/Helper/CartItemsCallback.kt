package com.example.foodapp.Helper

import com.example.foodapp.Domain.ItemsModel

interface CartItemsCallback {
    fun onCartItemsLoaded(items: ArrayList<ItemsModel>)
    fun onError(error: String)
}
