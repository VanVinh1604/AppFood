package com.example.foodapp.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Adapter.OrderDetailsAdapter
import com.example.foodapp.Domain.OrderDetails
import com.example.foodapp.R
import com.example.foodapp.ViewModel.MainViewModel
//import com.example.foodapp.Adapter.OrderDrinkAdapter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailsActivity :AppCompatActivity() {

    private lateinit var stepCircles: List<TextView>
    private var order: OrderDetails? = null
    private var deliveredDialogShown = false
    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        Log.d("OrderDetailsActivity", "onCreate called")

        order = intent.getSerializableExtra("order") as? OrderDetails
        var orderIdFromFCM = intent.getStringExtra("orderId")

// Nếu vẫn null, thử lấy từ extras khi app bị kill mở lại
        if (orderIdFromFCM == null) {
            val extras = intent.extras
            if (extras != null) {
                orderIdFromFCM = extras.getString("itemPushKey")
                Log.d("OrderDetailsActivity", "Fallback lấy từ extras: $orderIdFromFCM")
            }
        }

        val orderObj = intent.getSerializableExtra("order")
        Log.d("OrderDetailsActivity", "Received orderId from FCM: $orderIdFromFCM")
        Log.d("OrderDetailsActivity", "Received order object from intent: $orderObj")

        viewModel.orderDetailsLiveData.observe(this) { fetchedOrder ->
            Log.d("OrderDetailsActivity", "LiveData emit: $fetchedOrder")
            if (fetchedOrder != null) {
                order = fetchedOrder
                setupUI()
                observeDeliveryStatus()
            } else {
                Log.e("OrderDetails", "Order not found")
                finish()
            }
        }
        if (order == null && !orderIdFromFCM.isNullOrEmpty()) {
            viewModel.fetchOrderById(orderIdFromFCM)
        } else if (order != null) {
            setupUI()
        } else {
            Log.e("OrderDetails", "Không có order hoặc orderId")
            finish()
        }



        findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }

    private fun observeDeliveryStatus() {
        val customerId = order?.customerId ?: return
        val itemKey = order?.itemPushKey ?: return

        viewModel.listenToDeliveryStatus(customerId, itemKey)
            .observe(this) { status ->
                Log.d("OrderStatus", "deliveryStatus: $status")
                updateOrderStepUI(status)
            }
    }

    private fun setupUI() {
        initStepCircles()
        bindOrderData()
        observeDeliveryStatus()
        setupDrinkList()
    }

    private fun setupDrinkList() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerDrinkItems)

        val adapter = OrderDetailsAdapter(
            order?.drinkNames ?: emptyList(),
            order?.drinkImages ?: emptyList(),
            order?.drinkPrices ?: emptyList(),
            order?.drinkQuantities ?: emptyList(),
            order?.drinkSizes ?: emptyList()
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }


    private fun initStepCircles() {
        stepCircles = listOf(
            findViewById(R.id.step1Circle),
            findViewById(R.id.step2Circle),
            findViewById(R.id.step3Circle),
            findViewById(R.id.step4Circle)
        )
    }

    private fun bindOrderData() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        findViewById<TextView>(R.id.textOrderId).text = "Order ID: ${order?.itemPushKey ?: ""}"
        findViewById<TextView>(R.id.textCustomerInfo).text = "Customer: ${order?.customerName ?: ""}"
        findViewById<TextView>(R.id.textPhone).text = "Phone: ${order?.phoneNumber ?: ""}"
        findViewById<TextView>(R.id.textAddress).text = "Address: ${order?.address ?: ""}"
        findViewById<TextView>(R.id.textPaymentStatus).text = "Payment: ${order?.paymentStatus ?: "Unknown"}"
        findViewById<TextView>(R.id.textDeliveryStatus).text = "Status: ${order?.deliveryStatus ?: "Processing"}"
        findViewById<TextView>(R.id.textNote).text = "Note: ${order?.note ?: "None"}"
        findViewById<TextView>(R.id.textOrderTime).text =
            "Order time: ${dateFormat.format(Date(order?.currentTime ?: 0))}"

        val totalPrice = order?.totalPrice?.toDoubleOrNull() ?: 0.0
        val discount = order?.discountAmount?.toDoubleOrNull() ?: 0.0
        val originalPrice = totalPrice + discount

        findViewById<TextView>(R.id.textTotalPrice).text = String.format("Total: %.2f đ", totalPrice)
        findViewById<TextView>(R.id.textOriginalPrice).text = "Original: ${String.format("%.2f đ", originalPrice)}"

        val voucherView = findViewById<TextView>(R.id.textVoucherInfo)
        if (!order?.voucherCode.isNullOrBlank()) {
            voucherView.visibility = android.view.View.VISIBLE
            voucherView.text = "🎟️ Voucher: ${order?.voucherCode} - Discount ${String.format("%.2f đ", discount)}"
        } else {
            voucherView.visibility = android.view.View.GONE
        }
    }


    private fun updateOrderStepUI(status: String?) {
        stepCircles.forEach { it.setBackgroundResource(R.drawable.circle_gray) }
        val stepIndex = when (status?.trim()?.lowercase(Locale.ROOT)) {
            "unconfirmed" -> 0
            "in progress" -> 1
            "shipping" -> 2
            "delivered" -> 3
            null, "", "pending" -> 0
            else -> {
                Log.w("OrderStatus", "Unknown status: $status")

                return
            }
        }
        for (i in 0..stepIndex) {
            stepCircles[i].setBackgroundResource(R.drawable.circle_orange)
        }

        // Nếu đã giao và chưa hiện dialog lần nào ⇒ bật dialog
        if (stepIndex == 3 && !deliveredDialogShown) {
            deliveredDialogShown = true

            runOnUiThread {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Đơn hàng đã giao thành công 🎉")
                    .setMessage("Bạn có muốn đánh giá sản phẩm không?")
                    .setPositiveButton("Đánh giá ngay") { dialog, _ ->
                        dialog.dismiss()
                        navigateToReview()
                    }
                    .setNegativeButton("Để sau") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }
        }

    }
    private fun navigateToReview() {
        val intent = Intent(this, ReviewActivity::class.java)
        startActivity(intent)
    }


}