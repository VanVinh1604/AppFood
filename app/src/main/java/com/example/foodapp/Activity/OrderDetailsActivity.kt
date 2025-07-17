package com.example.foodapp.Activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Adapter.OrderDetailsAdapter
import com.example.foodapp.Domain.OrderDetails
import com.example.foodapp.R
//import com.example.foodapp.Adapter.OrderDrinkAdapter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailsActivity :AppCompatActivity() {

    private lateinit var stepCircles: List<TextView>
    private var order: OrderDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        order = intent.getSerializableExtra("order") as? OrderDetails
        if (order == null) {
            Log.e("OrderDetails", "Order is null")
            return
        }

        initStepCircles()
        bindOrderData()
        listenToDeliveryStatusUpdates()
        setupDrinkList()

        findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            finish()
        }
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

    private fun listenToDeliveryStatusUpdates() {
        val customerId = order?.customerId ?: return
        val itemKey = order?.itemPushKey ?: return

        val ref = FirebaseDatabase.getInstance()
            .getReference("Orders")
            .child(customerId)
            .child(itemKey)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("deliveryStatus").getValue(String::class.java)
                Log.d("OrderStatus", "deliveryStatus: $status")
                updateOrderStepUI(status)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderStatus", "Database error: ${error.message}")
            }
        })
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
    }
}