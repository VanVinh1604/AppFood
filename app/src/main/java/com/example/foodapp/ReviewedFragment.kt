package com.example.foodapp.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Adapter.ReviewedItemAdapter
import com.example.foodapp.Domain.OrderItem
import com.example.foodapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReviewedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewedItemAdapter
    private val reviewedList = mutableListOf<OrderItem>()
    private lateinit var emptyView: TextView
    private val reviewComments = mutableMapOf<String, Pair<String, Int>>() // drinkId-commentId -> (comment, rating)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reviewed, container, false)

        recyclerView = view.findViewById(R.id.reviewRecyclerView)
        emptyView = view.findViewById(R.id.emptyTextView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = ReviewedItemAdapter(requireContext(), reviewedList, reviewComments)
        recyclerView.adapter = adapter

        loadReviewedItems()

        return view
    }

    // Nếu không cần reload lại khi quay lại Fragment, có thể bỏ dòng này
    // override fun onResume() {
    //     super.onResume()
    //     loadReviewedItems()
    // }

    private fun loadReviewedItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        reviewedList.clear()
        reviewComments.clear()

        val commentsRef = FirebaseDatabase.getInstance().getReference("Comments")

        commentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userComments = mutableListOf<Triple<String, String, Long>>() // drinkId, commentId, timestamp

                for (drinkSnapshot in snapshot.children) {
                    val drinkId = drinkSnapshot.key ?: continue

                    for (commentSnapshot in drinkSnapshot.children) {
                        val customerID = commentSnapshot.child("customerID").getValue(String::class.java)

                        if (customerID == userId) {
                            val timestamp = commentSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                            val commentId = commentSnapshot.key ?: continue

                            userComments.add(Triple(drinkId, commentId, timestamp))

                            val comment = commentSnapshot.child("comment").getValue(String::class.java) ?: ""
                            val star = commentSnapshot.child("star").getValue(Int::class.java) ?: 0
                            reviewComments["$drinkId-$commentId"] = Pair(comment, star)
                        }
                    }
                }

                userComments.sortByDescending { it.third }
                loadProductInfoForComments(userComments)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReviewedFragment", "Error loading comments: ${error.message}")
                updateEmptyState()
            }
        })
    }

    private fun loadProductInfoForComments(comments: List<Triple<String, String, Long>>) {
        if (comments.isEmpty()) {
            updateEmptyState()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        var loadedCount = 0

        comments.forEach { (drinkId, commentId, timestamp) ->
            findProductInOrders(drinkId, userId, timestamp) { orderItem ->
                if (orderItem != null) {
                    val uniqueId = "$drinkId-$commentId"
                    val alreadyExists = reviewedList.any { it.drinkId == uniqueId }
                    if (!alreadyExists) {
                        val reviewedItem = orderItem.copy(
                            drinkId = uniqueId,
                            orderTime = timestamp
                        )
                        reviewedList.add(reviewedItem)
                    }
                }

                loadedCount++
                if (loadedCount == comments.size) {
                    Log.d("ReviewedFragment", "Reviewed List size: ${reviewedList.size}")
                    adapter.notifyDataSetChanged()
                    updateEmptyState()
                }
            }
        }
    }

    private fun findProductInOrders(
        drinkId: String,
        userId: String,
        reviewTime: Long,
        callback: (OrderItem?) -> Unit
    ) {
        val ordersRef = FirebaseDatabase.getInstance()
            .getReference("Orders")
            .child(userId)

        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var foundItem: OrderItem? = null
                var closestTimeDiff = Long.MAX_VALUE

                for (orderSnapshot in snapshot.children) {
                    val orderTime = orderSnapshot.child("currentTime").getValue(Long::class.java) ?: 0L

                    if (orderTime <= reviewTime) {
                        val timeDiff = reviewTime - orderTime

                        val drinkIds = mutableListOf<String>()
                        orderSnapshot.child("drinkIds").children.forEach {
                            drinkIds.add(it.getValue(String::class.java) ?: "")
                        }

                        val index = drinkIds.indexOf(drinkId)
                        if (index != -1 && timeDiff < closestTimeDiff) {
                            closestTimeDiff = timeDiff

                            val drinkNames = mutableListOf<String>()
                            val drinkImages = mutableListOf<String>()
                            val drinkPrices = mutableListOf<String>()
                            val drinkSizes = mutableListOf<String>()

                            orderSnapshot.child("drinkNames").children.forEach {
                                drinkNames.add(it.getValue(String::class.java) ?: "")
                            }
                            orderSnapshot.child("drinkImages").children.forEach {
                                drinkImages.add(it.getValue(String::class.java) ?: "")
                            }
                            orderSnapshot.child("drinkPrices").children.forEach {
                                drinkPrices.add(it.getValue(String::class.java) ?: "0")
                            }
                            orderSnapshot.child("drinkSizes").children.forEach {
                                drinkSizes.add(it.getValue(String::class.java) ?: "M")
                            }

                            foundItem = OrderItem(
                                drinkId = drinkId,
                                drinkName = drinkNames.getOrNull(index) ?: "Unknown",
                                drinkImage = drinkImages.getOrNull(index),
                                drinkPrice = drinkPrices.getOrNull(index)?.toDoubleOrNull(),
                                drinkSize = drinkSizes.getOrNull(index) ?: "M",
                                isReviewed = true,
                                orderTime = orderTime
                            )
                        }
                    }
                }

                if (foundItem == null) {
                    loadFromPopular(drinkId) { popularItem ->
                        callback(popularItem)
                    }
                } else {
                    callback(foundItem)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    private fun loadFromPopular(drinkId: String, callback: (OrderItem?) -> Unit) {
        val popularRef = FirebaseDatabase.getInstance()
            .getReference("Popular")
            .child(drinkId)

        popularRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val orderItem = OrderItem(
                    drinkId = drinkId,
                    drinkName = snapshot.child("drinkName").getValue(String::class.java) ?: "Unknown",
                    drinkImage = snapshot.child("drinkImage").getValue(String::class.java),
                    drinkPrice = snapshot.child("drinkPrice").getValue(Double::class.java),
                    drinkSize = "M",
                    isReviewed = true
                )
                callback(orderItem)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    private fun updateEmptyState() {
        if (reviewedList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyView.text = "Bạn chưa đánh giá sản phẩm nào"
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
}
