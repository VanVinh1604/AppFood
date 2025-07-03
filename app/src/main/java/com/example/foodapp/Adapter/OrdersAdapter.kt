package com.example.foodapp.Adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Domain.OrderDetails
import com.example.foodapp.R
import com.example.foodapp.databinding.ViewholderOrderBinding
import java.text.SimpleDateFormat
import java.util.*

class OrdersAdapter(private val onItemClick: (OrderDetails) -> Unit) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    private val orders: MutableList<OrderDetails> = mutableListOf()

    fun setOrders(newOrders: List<OrderDetails>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ViewholderOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(order: OrderDetails) {
            val context = binding.root.context

            binding.orderIdTxt.text = "Order ID: ${order.itemPushKey ?: "N/A"}"
            binding.nameTxt.text = order.customerName ?: "Unknown"
            binding.priceTxt.text = "Total: $${order.totalPrice ?: "0.0"}"

            if (!order.voucherCode.isNullOrEmpty() && !order.discountAmount.isNullOrEmpty()) {
                binding.voucherTxt.visibility = android.view.View.VISIBLE
                binding.voucherTxt.text =
                    "🎟️ Voucher: ${order.voucherCode} (−${order.discountAmount}đ)"
            } else {
                binding.voucherTxt.visibility = android.view.View.GONE
            }

            binding.addressTxt.text = "Address: ${order.address ?: "Unknown"}"

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.timeTxt.text = "Order Time: ${sdf.format(Date(order.currentTime))}"

            val circleDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setSize(24, 24)
            }

            // Order status
            when (order.deliveryStatus?.lowercase(Locale.getDefault())) {
                "in_progress" -> {
                    binding.statusTxt.text = "Status: In Progress"
                    circleDrawable.setColor(ContextCompat.getColor(context, R.color.yellow))
                }

                "shipping" -> {
                    binding.statusTxt.text = "Status: Shipping"
                    circleDrawable.setColor(ContextCompat.getColor(context, R.color.yellow))
                }

                "delivered" -> {
                    binding.statusTxt.text = "Status: Delivered"
                    circleDrawable.setColor(ContextCompat.getColor(context, R.color.green))
                }

                else -> {
                    binding.statusTxt.text = "Status: Unconfirmed"
                    circleDrawable.setColor(ContextCompat.getColor(context, R.color.red))
                }
            }

            binding.statusIndicator.background = circleDrawable

            // Click event
            binding.root.setOnClickListener {
                onItemClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ViewholderOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(orders[position])
    }
}