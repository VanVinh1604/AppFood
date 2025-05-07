package com.example.foodapp.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.Adapter.CartAdapter
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.Helper.CartItemsCallback
import com.example.foodapp.R
import com.example.foodapp.databinding.ActivityCartBinding
import com.example.foodapp.databinding.ViewholderCartBinding
import com.example.project1762.Helper.ManagmentCart
import com.uilover.project195.Helper.ChangeNumberItemsListener

class CartActivity : AppCompatActivity() {
    lateinit var binding: ActivityCartBinding
    lateinit var managmentCart: ManagmentCart
    private var tax: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)

        calculateCart()
        setVariable()
        initCartList()
    }

//    private fun initCartList() {
//        binding.apply {
//            listView.layoutManager =
//                LinearLayoutManager(this@CartActivity, LinearLayoutManager.VERTICAL, false)
//            listView.adapter = CartAdapter(
//                // GET LIST CART
//                managmentCart.getListCart(), this@CartActivity, object : ChangeNumberItemsListener {
//                    override fun onChanged() {
//                        calculateCart()
//                    }
//                }
//            )
//        }
//    }

    private fun initCartList() {
        binding.apply {
            listView.layoutManager =
                LinearLayoutManager(this@CartActivity, LinearLayoutManager.VERTICAL, false)
            val cart = ManagmentCart(this@CartActivity)
            cart.getListCartInFirebase(object : CartItemsCallback {
                override fun onCartItemsLoaded(items: ArrayList<ItemsModel>) {
                    listView.adapter = CartAdapter(items, this@CartActivity)
                }

                override fun onError(error: String) {
                    Toast.makeText(this@CartActivity, "Lỗi: $error", Toast.LENGTH_SHORT).show()
                }
            })

        }
    }

    private fun setVariable() {
        binding.backBtn.setOnClickListener { finish() }
    }

    private fun calculateCart() {
        val percentTax = 0.02
        val delivery = 15
        tax = Math.round((managmentCart.getTotalFee() * percentTax) * 100) / 100.0
        val total = Math.round((managmentCart.getTotalFee() + tax + delivery) * 100) / 100
        val itemTotal = Math.round(managmentCart.getTotalFee() * 100) / 100
        binding.apply {
            totalFeeTxt.text = "$$itemTotal"
            taxTxt.text = "$$tax"
            deliveryTxt.text = "$$delivery"
            totalTxt.text = "$$total"
        }
    }
}