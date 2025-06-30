package com.example.foodapp.Activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.example.foodapp.utils.dp

class CartActivity : AppCompatActivity() {
    lateinit var binding: ActivityCartBinding
    lateinit var managmentCart: ManagmentCart
    private var tax: Double = 0.0
    private var currentCartItems: ArrayList<ItemsModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)

//        calculateCart()
        setVariable()
        initCartList()
        setupCheckoutButton()
        setupBottomMenu()
    }
    override fun onResume() {
        super.onResume()
        initCartList()
    }


    private fun initCartList() {
        binding.apply {
            listView.layoutManager =
                LinearLayoutManager(this@CartActivity, LinearLayoutManager.VERTICAL, false)
            val cart = ManagmentCart(this@CartActivity)
            cart.getListCartInFirebase(object : CartItemsCallback {
                override fun onCartItemsLoaded(items: ArrayList<ItemsModel>) {
                    listView.adapter = CartAdapter(
                        items,
                        this@CartActivity,
                        changeNumberItemsListener = object : ChangeNumberItemsListener {
                            override fun onChanged() {

                                // Cập nhật lại tổng tiền khi có thay đổi
                                calculateCartFromList(items)
                            }
                        }
                    )

                    // Gọi tính tổng ngay sau khi load từ Firebase
                    calculateCartFromList(items)

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

    private fun setupCheckoutButton() {
        binding.CheckoutBtn.setOnClickListener {
            val cart = ManagmentCart(this)
            cart.getListCartInFirebase(object : CartItemsCallback {
                override fun onCartItemsLoaded(items: ArrayList<ItemsModel>) {

                    if (items.isEmpty()) {
                        Toast.makeText(this@CartActivity, "Hiện tại chưa có sản phẩm trong giỏ hàng", Toast.LENGTH_SHORT).show()
                        return
                    }

                    var total = 0.0
                    val drinkNames = mutableListOf<String>()
                    val drinkImages = mutableListOf<String>()
                    val drinkPrices = mutableListOf<String>()
                    val drinkQuantities = mutableListOf<Int>()
                    val drinkSizesList = mutableListOf<String>()

                    for (item in items) {
                        total += (item.drinkPrice ?: 0.0) * item.numberInCart
                        drinkNames.add(item.drinkName ?: "")
                        drinkImages.add(item.drinkImage ?: "")
                        drinkPrices.add((item.drinkPrice ?: 0.0).toString())
                        drinkQuantities.add(item.numberInCart)
                        drinkSizesList.add(item.size ?: "M")
                    }

                    val intent = Intent(this@CartActivity, PaymentActivity::class.java)
                    intent.putExtra("totalPrice", total.toString())
                    intent.putStringArrayListExtra("drinkNames", ArrayList(drinkNames))
                    intent.putStringArrayListExtra("drinkImages", ArrayList(drinkImages))
                    intent.putStringArrayListExtra("drinkPrices", ArrayList(drinkPrices))
                    intent.putIntegerArrayListExtra("drinkQuantities", ArrayList(drinkQuantities))
                    intent.putStringArrayListExtra("drinkSizes", ArrayList(drinkSizesList))

                    startActivity(intent)
                }

                override fun onError(error: String) {
                    Toast.makeText(this@CartActivity, "Lỗi: $error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun calculateCartFromList(items: List<ItemsModel>) {
        val percentTax = 0.02
        val delivery = 15
        var fee = 0.0

        for (item in items) {
            fee += (item.drinkPrice ?: 0.0) * item.numberInCart
        }

        val tax = Math.round((fee * percentTax) * 100) / 100.0
        val total = Math.round((fee + tax + delivery) * 100) / 100
        val itemTotal = Math.round(fee * 100) / 100

        binding.apply {
            totalFeeTxt.text = "$$itemTotal"
            taxTxt.text = "$$tax"
            deliveryTxt.text = "$$delivery"
            totalTxt.text = "$$total"
        }
    }

    private fun setupBottomMenu() {
        val rootView = findViewById<View>(R.id.bottomMenuInclude)

        // CART (đang active nên highlight)
        val cartIcon = rootView.findViewById<ImageView>(R.id.imagecart)
        val cartText = rootView.findViewById<TextView>(R.id.textcart)
        cartIcon.setColorFilter(getColor(R.color.orange))
        cartIcon.layoutParams.width = 28.dp
        cartIcon.layoutParams.height = 28.dp
        cartIcon.requestLayout()
        cartText.setTextColor(getColor(R.color.orange))
        cartText.setTypeface(null, Typeface.BOLD)

        // HOME
        val homeBtn = rootView.findViewById<View>(R.id.HomeBtn)
        homeBtn.setOnClickListener {
            val intent = Intent(this@CartActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // FAVORITE
        val favoriteBtn = rootView.findViewById<View>(R.id.favoriteBtn)
        favoriteBtn.setOnClickListener {
 //           startActivity(Intent(this, FavoriteActivity::class.java))
            finish()
        }

        // ORDER
        val orderBtn = rootView.findViewById<View>(R.id.orderBtn)
        orderBtn.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
            finish()
        }

        // PROFILE
        val profileBtn = rootView.findViewById<View>(R.id.profileBtn)
        profileBtn.setOnClickListener {
    //        startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

}