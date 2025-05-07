package com.example.project1762.Helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.Helper.CartItemsCallback
import com.example.foodapp.Helper.DeviceUidHelper
import com.example.foodapp.Helper.TinyDB
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID
import com.uilover.project195.Helper.ChangeNumberItemsListener


class ManagmentCart(val context: Context) {

    private val tinyDB = TinyDB(context)

    fun insertItems(item: ItemsModel) {
//        var listItem = getListCart()
//        val existAlready = listItem.any { it.title == item.title }
//        val index = listItem.indexOfFirst { it.title == item.title }
//
//        if (existAlready) {
//            listItem[index].numberInCart = item.numberInCart
//        } else {
//            listItem.add(item)
//        }
//        tinyDB.putListObject("CartList", listItem)
//        updateItemInFirebase(item)
//        Log.d("Insert item in cart", "${item}")
//        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<ItemsModel> {
        return tinyDB.getListObject("CartList") ?: arrayListOf()
    }

    // get list cart in Firebase
    fun getListCartInFirebase(callback: CartItemsCallback) {
        val userId = DeviceUidHelper.getDeviceUid(context)
        val ref = FirebaseDatabase.getInstance().getReference("Cart").child(userId)

        ref.get().addOnSuccessListener { snapshot ->
            val list = ArrayList<ItemsModel>()
            for (child in snapshot.children) {
                val item = child.getValue(ItemsModel::class.java)
                if (item != null) {
                    list.add(item)
                }
            }
            callback.onCartItemsLoaded(list)
        }.addOnFailureListener { exception ->
            callback.onError(exception.message ?: "Failed to load cart items")
        }
    }

    fun minusItem(
        listItems: ArrayList<ItemsModel>,
        position: Int,
        listener: ChangeNumberItemsListener
    ) {
//        val item = listItems[position]
//        if (item.numberInCart == 1) {
//            removeItemInFirebase(item.title)
//            listItems.removeAt(position)
//        } else {
//            item.numberInCart--
//            updateItemInFirebase(item)
//        }
//        tinyDB.putListObject("CartList", listItems)
//        listener.onChanged()
    }


    fun romveItem(
        listItems: ArrayList<ItemsModel>,
        position: Int,
        listener: ChangeNumberItemsListener
    ) {
        val item = listItems[position]
        listItems.removeAt(position)
        tinyDB.putListObject("CartList", listItems)
      //  removeItemInFirebase(item.title)
        listener.onChanged()
    }


    fun plusItem(
        listItems: ArrayList<ItemsModel>,
        position: Int,
        listener: ChangeNumberItemsListener
    ) {
     //   listItems[position].numberInCart++
        tinyDB.putListObject("CartList", listItems)
//        updateItemInFirebase(listItems[position])
        listener.onChanged()
    }

    fun getTotalFee(): Double {
        val listItem = getListCart()
        var fee = 0.0
        for (item in listItem) {
     //       fee += item.price * item.numberInCart
        }
        return fee
    }

//    private fun updateItemInFirebase(item: ItemsModel) {
//        val userId = DeviceUidHelper.getDeviceUid(context)
//        val idCart = UUID.randomUUID().toString()
//        val ref = FirebaseDatabase.getInstance().getReference("Cart").child(userId)
//    //        .child(item.title)
//        ref.setValue(item)
//        Log.d("Update Item In Firebase", "${item}")
//    }

    private fun removeItemInFirebase(itemId: String) {
        val userId = DeviceUidHelper.getDeviceUid(context)
        val ref = FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child(userId)
            .child(itemId)
        ref.removeValue()
        Log.d("Remove Item in firebase", "${itemId}")
    }
}