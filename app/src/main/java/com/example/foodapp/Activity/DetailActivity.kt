package com.example.foodapp.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.foodapp.Adapter.CommentAdapter
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.R
import com.example.foodapp.Repository.MainRepository
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.databinding.ActivityDetailBinding
import com.example.project1762.Helper.ManagmentCart

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

        bundle()
        initSizeList()
        setupCommentSection()
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
        binding.apply{
            item=intent.getSerializableExtra("object")as ItemsModel

            Glide.with(this@DetailActivity)
                .load(item.drinkImage?.get(0))
                .into(binding.picMain)

            titleTxt.text=item.drinkName
            descriptionTxt.text=item.drinkDescription
            priceTxt.text="$"+item.drinkPrice
            ingredientTxt.text=item.drinkExtra
      //      ratingTxt.text=item.rating.toString()



//            addToCartBtn.setOnClickListener {
//                item.numberInCart=Integer.valueOf(
//                    numberItemTxt.text.toString()
//                )
//                item.size = selectedSize
//                managmentCart.insertItems(item)
//            }

            backBtn.setOnClickListener {
                finish()
            }

//            plusCart.setOnClickListener {
//                numberItemTxt.text=(item.numberInCart+1).toString()
//                item.numberInCart++
//            }
//
//            minusCart.setOnClickListener {
//                if (item.numberInCart > 0) {
//                    numberItemTxt.text = (item.numberInCart - 1).toString()
//                    item.numberInCart--
//                }
//            }
        }
    }
    private fun setupCommentSection() {
        binding.progressBarComment.visibility = View.VISIBLE
        binding.recycleviewComment.visibility = View.GONE

        commentViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        commentAdapter = CommentAdapter(emptyList())
        binding.recycleviewComment.layoutManager = LinearLayoutManager(this)
        binding.recycleviewComment.adapter = commentAdapter

        item.drinkId?.let {
            commentViewModel.loadComment(it)
        }

        commentViewModel.loadComment(item.drinkId ?: "").observe(this) { commentList ->

            binding.progressBarComment.visibility = View.GONE
            binding.recycleviewComment.visibility = View.VISIBLE
            commentAdapter.setData(commentList)
        }
    }
}