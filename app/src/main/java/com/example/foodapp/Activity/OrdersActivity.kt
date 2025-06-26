package com.example.foodapp.Activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.databinding.ActivityOrdersBinding
import com.example.foodapp.utils.dp
import android.graphics.Typeface
import com.example.foodapp.Adapter.OrdersAdapter


class OrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrdersBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = OrdersAdapter { selectedOrder ->
//         val intent = Intent(this, OrderDetailsActivity::class.java)
//            intent.putExtra("order", selectedOrder) // OrderDetails phải là Serializable hoặc Parcelable
//            startActivity(intent)
        }

        binding.orderRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.orderRecyclerView.adapter = adapter

        observeOrderList()

        binding.backButton.setOnClickListener {
            finish()
        }
        setupBottomMenu()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = OrdersAdapter { selectedOrder ->
            val intent = Intent(this, OrderDetailsActivity::class.java)
            intent.putExtra("order", selectedOrder)
            startActivity(intent)
        }

        binding.orderRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.orderRecyclerView.adapter = adapter
    }

    private fun observeOrderList() {
        viewModel.getOrderHistory().observe(this) { orderList ->
            if (orderList.isNullOrEmpty()) {
                binding.orderRecyclerView.visibility = View.GONE
                binding.emptyOrderView.visibility = View.VISIBLE
            } else {
                binding.emptyOrderView.visibility = View.GONE
                binding.orderRecyclerView.visibility = View.VISIBLE
                adapter.setOrders(orderList)
            }
        }
    }

    private fun setupBottomMenu() {
        val rootView = findViewById<View>(R.id.bottomMenuInclude)

        // ORDER đang active (hiển thị cam + to)
        val orderIcon = rootView.findViewById<ImageView>(R.id.imageorder)
        val orderText = rootView.findViewById<TextView>(R.id.textorder)
        orderIcon.setColorFilter(getColor(R.color.orange))
        orderIcon.layoutParams.width = 28.dp
        orderIcon.layoutParams.height = 28.dp
        orderIcon.requestLayout()
        orderText.setTextColor(getColor(R.color.orange))
        orderText.setTypeface(null, Typeface.BOLD)

        // HOME
        val homeBtn = rootView.findViewById<View>(R.id.HomeBtn)
        homeBtn.setOnClickListener {
            val intent = Intent(this@OrdersActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // FAVORITE
        val favoriteBtn = rootView.findViewById<View>(R.id.favoriteBtn)
        favoriteBtn.setOnClickListener {
            // startActivity(Intent(this, FavoriteActivity::class.java))
            finish()
        }

        // CART
        val cartBtn = rootView.findViewById<View>(R.id.cartBtn)
        cartBtn.setOnClickListener {
            val intent = Intent(this@OrdersActivity, CartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // PROFILE
        val profileBtn = rootView.findViewById<View>(R.id.profileBtn)
        profileBtn.setOnClickListener {
            // startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

}
