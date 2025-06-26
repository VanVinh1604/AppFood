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

        findViewById<TextView>(R.id.textOrderId).text = "Mã đơn hàng: ${order?.itemPushKey ?: ""}"
        findViewById<TextView>(R.id.textCustomerInfo).text = "Khách hàng: ${order?.customerName ?: ""}"
        findViewById<TextView>(R.id.textPhone).text = "Số điện thoại: ${order?.phoneNumber ?: ""}"
        findViewById<TextView>(R.id.textAddress).text = "Địa chỉ: ${order?.address ?: ""}"
        findViewById<TextView>(R.id.textTotalPrice).text = "Tổng tiền: ${order?.totalPrice ?: ""} đ"
        findViewById<TextView>(R.id.textPaymentStatus).text = "Thanh toán: ${order?.paymentStatus ?: "Chưa xác định"}"
        findViewById<TextView>(R.id.textDeliveryStatus).text = "Tình trạng: ${order?.deliveryStatus ?: "Đang xử lý"}"
        findViewById<TextView>(R.id.textNote).text = "Ghi chú: ${order?.note ?: "Không có"}"
        findViewById<TextView>(R.id.textOrderTime).text =
            "Ngày đặt: ${dateFormat.format(Date(order?.currentTime ?: 0))}"
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
        // Reset về màu xám cho tất cả
        stepCircles.forEach { it.setBackgroundResource(R.drawable.circle_gray) }

        // Trích trạng thái và xác định bước tương ứng
        val stepIndex = when (status?.trim()?.lowercase(Locale.ROOT)) {
            "unconfirmed" -> 0
            "in progress" -> 1
            "shipping" -> 2
            "delivered" -> 3
            null, "", "pending" -> 0
            else -> {
                Log.w("OrderStatus", "Trạng thái không xác định: $status")
                return
            }
        }
        for (i in 0..stepIndex) {
            stepCircles[i].setBackgroundResource(R.drawable.circle_orange)
        }
    }
}
