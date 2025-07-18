package com.example.foodapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Domain.VouchersModel
import com.example.foodapp.R

class VoucherAdapter(
    private val vouchers: List<VouchersModel>,
    private val onItemClick: (VouchersModel) -> Unit
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    inner class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val code: TextView = itemView.findViewById(R.id.voucherCode)
        val desc: TextView = itemView.findViewById(R.id.voucherDesc)
        val expiry: TextView = itemView.findViewById(R.id.voucherExpiry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = vouchers[position]
        holder.code.text = voucher.code ?: "No Code"
        holder.desc.text = "Giảm ${voucher.discountPercent}% tối đa ${voucher.maxDiscount}k"
        holder.expiry.text = "HSD: ${voucher.expiresAt ?: "Không xác định"}"

        holder.itemView.setOnClickListener {
            onItemClick(voucher)
        }
    }

    override fun getItemCount(): Int = vouchers.size
}
