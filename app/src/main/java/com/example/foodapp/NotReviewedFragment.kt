package com.example.foodapp.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Adapter.ReviewItemAdapter
import com.example.foodapp.Domain.OrderItem
import com.example.foodapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotReviewedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewItemAdapter
    private lateinit var reviewList: MutableList<OrderItem>
    private lateinit var emptyView: TextView
    // Map để track orderId và itemIndex cho mỗi OrderItem
    private val orderTrackingMap = mutableMapOf<Int, Pair<String, Int>>() // position -> (orderId, itemIndex)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_not_reviewed, container, false)

        recyclerView = view.findViewById(R.id.reviewRecyclerView)
        emptyView = view.findViewById(R.id.emptyTextView)

        recyclerView.layoutManager = LinearLayoutManager(context)

        reviewList = mutableListOf()
        adapter = ReviewItemAdapter(requireContext(), reviewList)
        recyclerView.adapter = adapter

        // Set listener để xử lý khi review thành công
        adapter.setOnReviewSubmittedListener(object : ReviewItemAdapter.OnReviewSubmittedListener {
            override fun onReviewSubmitted(position: Int) {
                if (position < reviewList.size) {
                    val item = reviewList[position]

                    // Update isReviewed trong Firebase cho order cụ thể
                    val orderInfo = orderTrackingMap[position]
                    if (orderInfo != null) {
                        updateItemReviewStatus(orderInfo.first, orderInfo.second)
                    }

                    // Remove item khỏi list
                    reviewList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    adapter.notifyItemRangeChanged(position, reviewList.size)

                    // Update tracking map
                    updateTrackingMap(position)

                    updateEmptyState()
                    Toast.makeText(context, "Review successful!", Toast.LENGTH_SHORT).show()
                }
            }
        })

        loadOrdersFromFirebase()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Reload khi quay lại fragment
        loadOrdersFromFirebase()
    }

    private fun loadOrdersFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Orders").child(userId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reviewList.clear()
                orderTrackingMap.clear()
                var currentPosition = 0

                for (orderSnap in snapshot.children) {
                    val orderId = orderSnap.key ?: continue
                    val deliveryStatus = orderSnap.child("deliveryStatus").getValue(String::class.java)
                    val orderTime = orderSnap.child("currentTime").getValue(Long::class.java) ?: 0L

                    Log.d("NotReviewedFragment", "Order $orderId: deliveryStatus=$deliveryStatus")

                    // Chỉ lấy order đã giao hàng
                    if (deliveryStatus == "Delivered") {
                        // Check xem order này có field reviewedItems không
                        val reviewedItems = mutableMapOf<String, Boolean>()
                        orderSnap.child("reviewedItems").children.forEach {
                            reviewedItems[it.key ?: ""] = it.getValue(Boolean::class.java) ?: false
                        }

                        // Lấy arrays từ order
                        val drinkNames = mutableListOf<String>()
                        val drinkImages = mutableListOf<String>()
                        val drinkPrices = mutableListOf<String>()
                        val drinkQuantities = mutableListOf<Int>()
                        val drinkSizes = mutableListOf<String>()
                        val drinkIds = mutableListOf<String>()

                        // Đọc data
                        orderSnap.child("drinkNames").children.forEach {
                            drinkNames.add(it.getValue(String::class.java) ?: "")
                        }
                        orderSnap.child("drinkImages").children.forEach {
                            drinkImages.add(it.getValue(String::class.java) ?: "")
                        }
                        orderSnap.child("drinkPrices").children.forEach {
                            drinkPrices.add(it.getValue(String::class.java) ?: "0")
                        }
                        orderSnap.child("drinkQuantities").children.forEach {
                            drinkQuantities.add(it.getValue(Int::class.java) ?: 1)
                        }
                        orderSnap.child("drinkSizes").children.forEach {
                            drinkSizes.add(it.getValue(String::class.java) ?: "M")
                        }
                        orderSnap.child("drinkIds").children.forEach {
                            drinkIds.add(it.getValue(String::class.java) ?: "")
                        }

                        // Tạo OrderItem cho mỗi drink
                        for (i in drinkNames.indices) {
                            val drinkId = drinkIds.getOrNull(i)

                            // Kiểm tra xem item này trong order này đã được review chưa
                            val itemKey = "item_$i"
                            val isItemReviewed = reviewedItems[itemKey] ?: false

                            if (!drinkId.isNullOrEmpty() && !isItemReviewed) {
                                val orderItem = OrderItem(
                                    drinkId = drinkId,
                                    drinkName = drinkNames[i],
                                    drinkImage = drinkImages.getOrNull(i),
                                    drinkPrice = drinkPrices.getOrNull(i)?.toDoubleOrNull(),
                                    drinkQuantity = drinkQuantities.getOrNull(i) ?: 1,
                                    drinkSize = drinkSizes.getOrNull(i) ?: "M",
                                    isReviewed = false,
                                    orderTime = orderTime
                                )

                                reviewList.add(orderItem)
                                orderTrackingMap[currentPosition] = Pair(orderId, i)
                                currentPosition++

                                Log.d("NotReviewedFragment", "Added item: ${orderItem.drinkName}, drinkId: ${orderItem.drinkId}")
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged()
                updateEmptyState()

                Log.d("NotReviewedFragment", "Total items to review: ${reviewList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotReviewedFragment", "Database error: ${error.message}")
                Toast.makeText(context, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateItemReviewStatus(orderId: String, itemIndex: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val orderRef = FirebaseDatabase.getInstance()
            .getReference("Orders")
            .child(userId)
            .child(orderId)

        // Update reviewedItems field
        orderRef.child("reviewedItems").child("item_$itemIndex").setValue(true)
            .addOnSuccessListener {
                Log.d("NotReviewedFragment", "Updated review status for order $orderId, item $itemIndex")
            }
            .addOnFailureListener { e ->
                Log.e("NotReviewedFragment", "Failed to update review status: ${e.message}")
            }
    }

    private fun updateTrackingMap(removedPosition: Int) {
        // Update positions in tracking map after removal
        val newMap = mutableMapOf<Int, Pair<String, Int>>()
        orderTrackingMap.forEach { (position, value) ->
            if (position > removedPosition) {
                newMap[position - 1] = value
            } else if (position < removedPosition) {
                newMap[position] = value
            }
        }
        orderTrackingMap.clear()
        orderTrackingMap.putAll(newMap)
    }

    private fun updateEmptyState() {
        if (reviewList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyView.text = "There are no products to review"
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
}