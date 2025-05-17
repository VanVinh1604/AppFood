package com.example.foodapp.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.foodapp.Adapter.CommentAdapter
import com.example.foodapp.Domain.CommentModel
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.R
import com.example.foodapp.Repository.MainRepository
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.databinding.ActivityDetailBinding
import com.example.project1762.Helper.ManagmentCart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityDetailBinding
    private lateinit var item:ItemsModel
    private lateinit var managmentCart: ManagmentCart
    private var selectedSize: String = "Small"
    private lateinit var commentViewModel: MainViewModel
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart=ManagmentCart(this)
        commentViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        commentAdapter = CommentAdapter(emptyList())
        binding.recycleviewComment.layoutManager = LinearLayoutManager(this)
        binding.recycleviewComment.adapter = commentAdapter

        bundle()
        initSizeList()

        setupCommentSection()
        loadComments()

    }

    private fun initSizeList(){
        binding.apply {
            smallBtn.setBackgroundResource(R.drawable.stroke_brown_bg)
            mediumBtn.setBackgroundResource(0) // set màu nền xám hoặc default
            largeBtn.setBackgroundResource(0)

            selectedSize = "Small"

            smallBtn.setOnClickListener {
                selectedSize = "Small"
                smallBtn.setBackgroundResource(R.drawable.stroke_brown_bg)
                mediumBtn.setBackgroundResource(0)
                largeBtn.setBackgroundResource(0)
            }
            mediumBtn.setOnClickListener {
                selectedSize = "Medium"
                smallBtn.setBackgroundResource(0)
                mediumBtn.setBackgroundResource(R.drawable.stroke_brown_bg)
                largeBtn.setBackgroundResource(0)
            }
            largeBtn.setOnClickListener {
                selectedSize = "Large"
                smallBtn.setBackgroundResource(0)
                mediumBtn.setBackgroundResource(0)
                largeBtn.setBackgroundResource(R.drawable.stroke_brown_bg)
            }
        }
    }
    private fun bundle(){
        binding.apply {
            item = intent.getSerializableExtra("object") as ItemsModel


            Glide.with(this@DetailActivity)
                .load(item.drinkImage)
                .into(binding.picMain)

            titleTxt.text = item.drinkName
            descriptionTxt.text = item.drinkDescription
            priceTxt.text = "$" + item.drinkPrice
            ingredientTxt.text = item.drinkExtra
//           ratingTxt.text = item.rating.toString()
            item.drinkId?.let { loadCurrentRating(it) }



            binding.addToCartBtn.setOnClickListener {
                val number = binding.numberItemTxt.text.toString().toIntOrNull() ?: 1

                val itemToCart = item.copy(
                    numberInCart = number,
                    size = selectedSize
                )

                managmentCart.insertItemToCart(itemToCart)
            }


            backBtn.setOnClickListener {
                finish()
            }

            plusCart.setOnClickListener {
                numberItemTxt.text = (item.numberInCart + 1).toString()
                item.numberInCart++
            }

            minusCart.setOnClickListener {
                if (item.numberInCart > 0) {
                    numberItemTxt.text = (item.numberInCart - 1).toString()
                    item.numberInCart--
                }
            }
        }
    }
    private fun setupCommentSection() {
        binding.commentBtn.setOnClickListener {
            val commentText = binding.commentTxt.text.toString().trim()
            val ratingValue = binding.ratingComment.rating
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId == null) {
                Toast.makeText(this, "You must login to post a review.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (commentText.isEmpty() || ratingValue <= 0f)  {
                Toast.makeText(this, "Please enter review content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val commentRef = FirebaseDatabase.getInstance().getReference("Comments")
                .child(item.drinkId!!)
                .child(userId)

            // Check if user already rated
            commentRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // User has already rated
                    Toast.makeText(this, "You have already submitted a review for this product.", Toast.LENGTH_SHORT).show()
                } else {
                    // No existing review, allow to submit
                    val comment = CommentModel(
                        customerID = userId,
                        comment = commentText,
                        star = ratingValue,
                        title = "User Reviews",
                        createdAt = System.currentTimeMillis()
                    )

                    commentRef.setValue(comment)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Review sent successfully", Toast.LENGTH_SHORT).show()
                            binding.commentTxt.text.clear()
                            binding.ratingComment.rating = 0f
                            commentViewModel.loadComment(item.drinkId ?: "")
                           loadComments()
                           updateAverageRatingToItem(item.drinkId ?: "")
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Send failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to check previous reviews: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAverageRatingToItem(drinkId: String) {
        val commentRef = FirebaseDatabase.getInstance().getReference("Comments").child(drinkId)

        commentRef.get().addOnSuccessListener { dataSnapshot ->
            var totalStars = 0f
            var count = 0

            for (commentSnapshot in dataSnapshot.children) {
                val comment = commentSnapshot.getValue(CommentModel::class.java)
                comment?.let {
                    totalStars += it.star ?: 0f
                    count++
                }
            }

            val averageRating = if (count > 0) totalStars / count else 0f

            val itemRef = FirebaseDatabase.getInstance().getReference("Items").child(drinkId)
            itemRef.child("rating").setValue(averageRating)
                .addOnSuccessListener {
                    itemRef.child("rating").get()
                        .addOnSuccessListener { ratingSnapshot ->
                            val latestRating = ratingSnapshot.getValue(Float::class.java) ?: 0f
                            binding.ratingTxt.text = String.format("%.1f", latestRating)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Không thể cập nhật rating: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Không thể đọc comment: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadComments() {
        val drinkId = item.drinkId ?: return

        commentViewModel.loadComment(drinkId).observe(this) { commentList ->
            commentAdapter.setData(commentList)
            updateAverageRatingToItem(drinkId)
        }
    }

    private fun loadCurrentRating(drinkId: String) {
        val itemRef = FirebaseDatabase.getInstance().getReference("Items").child(drinkId)
        itemRef.child("rating").get()
            .addOnSuccessListener { snapshot ->
                val rating = snapshot.getValue(Float::class.java) ?: 0f
                binding.ratingTxt.text = String.format("%.1f", rating)
            }
            .addOnFailureListener {
                binding.ratingTxt.text = "N/A"
            }
    }
}