package com.example.foodapp.Activity

import android.app.DatePickerDialog
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
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.example.foodapp.Adapter.OrdersAdapter
import com.example.foodapp.Domain.OrderDetails
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class OrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrdersBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: OrdersAdapter
    private var allOrders: List<OrderDetails> = emptyList()
//    private var dateFilterLiveData: LiveData<List<OrderDetails>>? = null
    private var currentStatusFilter = "new"
    private var selectedDate: String? = null



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

        binding.filterNew.setOnClickListener {
            showOrders("new")
            updateFilterUI("new")
        }

        binding.clearDateFilterBtn.setOnClickListener {
            selectedDate = null
            Toast.makeText(this, "Đã xóa lọc ngày", Toast.LENGTH_SHORT).show()
            showOrders(currentStatusFilter)
        }


        binding.filterDelivered.setOnClickListener {
            showOrders("delivered")
            updateFilterUI("delivered")
        }
        val dateFilterTextView = findViewById<TextView>(R.id.dateFilterTextView)
        dateFilterTextView.setOnClickListener { showDatePicker() }
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
            allOrders = orderList ?: emptyList()
            if (allOrders.isEmpty()) {
                binding.orderRecyclerView.visibility = View.GONE
                binding.emptyOrderView.visibility = View.VISIBLE
            } else {
                binding.emptyOrderView.visibility = View.GONE
                binding.orderRecyclerView.visibility = View.VISIBLE

                showOrders("new")
                updateFilterUI("new")
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                Toast.makeText(this, "Lọc theo ngày: $selectedDate", Toast.LENGTH_SHORT).show()

                // Gọi lọc lại theo trạng thái hiện tại + ngày mới chọn
                showOrders(currentStatusFilter)
            },
            year, month, day
        )
        datePickerDialog.show()
    }



    private fun showOrders(filter: String) {
        currentStatusFilter = filter

        val filtered = allOrders.filter { order ->
            val matchStatus = when (filter) {
                "delivered" -> order.deliveryStatus?.lowercase() == "delivered"
                "new" -> order.deliveryStatus?.lowercase() != "delivered"
                else -> true
            }

            val matchDate = selectedDate?.let { dateStr ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.time = sdf.parse(dateStr)!!

                val orderCalendar = Calendar.getInstance()
                orderCalendar.timeInMillis = order.currentTime

                orderCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                        orderCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                        orderCalendar.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH)
            } ?: true // nếu chưa chọn ngày thì mặc định là đúng

            matchStatus && matchDate
        }

        adapter.setOrders(filtered)
        binding.orderRecyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        binding.emptyOrderView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE

        binding.clearDateFilterBtn.visibility = if (selectedDate != null) View.VISIBLE else View.GONE
    }


    private fun updateFilterUI(active: String) {
        if (active == "new") {
            binding.filterNew.setBackgroundResource(R.drawable.dark_brown_bg)
            binding.filterNew.setTextColor(getColor(R.color.white))

            binding.filterDelivered.setBackgroundResource(R.drawable.white_bg)
            binding.filterDelivered.setTextColor(getColor(R.color.darkBrown))
        } else {
            binding.filterDelivered.setBackgroundResource(R.drawable.dark_brown_bg)
            binding.filterDelivered.setTextColor(getColor(R.color.white))

            binding.filterNew.setBackgroundResource(R.drawable.white_bg)
            binding.filterNew.setTextColor(getColor(R.color.darkBrown))
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
             startActivity(Intent(this, FavoritesActivity::class.java))
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
             startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

}
