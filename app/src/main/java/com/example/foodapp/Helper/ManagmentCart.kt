package com.example.project1762.Helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.foodapp.Domain.CartModel
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.Helper.CartItemsCallback
import com.example.foodapp.Helper.DeviceUidHelper
import com.example.foodapp.Helper.TinyDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID
import com.uilover.project195.Helper.ChangeNumberItemsListener


class ManagmentCart(val context: Context) {

    private val tinyDB = TinyDB(context)
    private val firebaseAuth = FirebaseAuth.getInstance()
    init {
        tinyDB.remove("CartList")
        tinyDB.remove("CartModel")
    }

    fun insertItemToCart(newItem: ItemsModel) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Please login", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Cart").child(userId)
        ref.get().addOnSuccessListener { snapshot ->
            val currentCart = snapshot.getValue(CartModel::class.java)
            val cartList = currentCart?.listItem?.toMutableList() ?: mutableListOf()

            // Tạo id duy nhất để kiểm tra trùng
            val itemId = generateCartId(newItem)
            val index = cartList.indexOfFirst { generateCartId(it) == itemId }

            if (index != -1) {
                cartList[index] = cartList[index].copy(
                    numberInCart = cartList[index].numberInCart + newItem.numberInCart
                )
            } else {
                cartList.add(newItem)
            }

            val updatedCart = CartModel(
                listItem = cartList
            )
            ref.setValue(updatedCart)
            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Error accessing cart", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCartModel(): CartModel? {
        return tinyDB.getObject("CartModel", CartModel::class.java)
    }

    fun getListCart(): ArrayList<ItemsModel> {
        return tinyDB.getListObject("CartList") ?: arrayListOf()
    }

    // get list cart in Firebase
    fun getListCartInFirebase(callback: CartItemsCallback) {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            callback.onError("User not logged in")
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Cart").child(userId)

        ref.get().addOnSuccessListener { snapshot ->
            val cart = snapshot.getValue(CartModel::class.java)
            val list = cart?.listItem?.toCollection(ArrayList()) ?: arrayListOf()
            callback.onCartItemsLoaded(list)
        }.addOnFailureListener {
            callback.onError(it.message ?: "Failed to load cart items")
        }
    }

    fun minusItem(
        listItems: ArrayList<ItemsModel>,
        position: Int,
        listener: ChangeNumberItemsListener
    ) {
        val item = listItems[position]
        val cartId = generateCartId(item)
        if (item.numberInCart == 1) {
            removeItemInFirebase(cartId)
            listItems.removeAt(position)
        } else {
            item.numberInCart--
            updateCartListInFirebase(listItems)
        }
        tinyDB.putListObject("CartList", listItems)
        listener.onChanged()
    }


    fun romveItem(
        listItems: ArrayList<ItemsModel>,
        position: Int,
        listener: ChangeNumberItemsListener
    ) {
        listItems.removeAt(position)
        updateCartListInFirebase(listItems) // cập nhật lên Firebase
        tinyDB.putListObject("CartList", listItems)
        listener.onChanged()
    }


    fun plusItem(
        listItems: ArrayList<ItemsModel>,
        position: Int,
        listener: ChangeNumberItemsListener
    ) {
        listItems[position].numberInCart++
        tinyDB.putListObject("CartList", listItems)
        updateCartListInFirebase(listItems)
        listener.onChanged()
    }

    fun getTotalFee(): Double {
        val listItem = getListCart()
        var fee = 0.0
        for (item in listItem) {
            fee += (item.drinkPrice ?: 0.0) * item.numberInCart
        }
        return fee
    }

    private fun updateCartListInFirebase(cartList: List<ItemsModel>) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        val cartModel = CartModel(

            drinkQuantity = 0,
            listItem = cartList
        )

        val ref = FirebaseDatabase.getInstance().getReference("Cart").child(userId)
        ref.setValue(cartModel)
    }

    private fun generateCartId(item: ItemsModel): String {
        val drinkId = item.drinkId ?: ""
        val size = item.size ?: ""
        return "${drinkId}_${size}"
    }

    private fun removeItemInFirebase(cartId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w("Cart", "User not logged in")
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Cart").child(userId).child(cartId)
        ref.removeValue()
        Log.d("Remove Item in firebase", cartId)
    }
}