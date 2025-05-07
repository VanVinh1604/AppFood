package com.example.foodapp.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.foodapp.Adapter.CategoryAdapter
import com.example.foodapp.Adapter.PopularAdapter
import com.example.foodapp.Domain.CategoryModel
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.R
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()
//    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var popularAdapter: PopularAdapter
//
//    private var categoryList = listOf<CategoryModel>()  // Lưu data để search
    private var popularList = listOf<ItemsModel>()    // Lưu data để search
//
//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.searchView.isIconified = false

        initBanner()
        initCategory()
        initPopular()
        initBottomMenu()
   //     initSearch()
    }
//    private fun initSearch() {
//        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let {
//                    filterItems(it)
//                }
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                newText?.let {
//                    filterItems(it)
//                }
//                return false
//            }
//        })
//    }

//    private fun filterItems(query: String) {
//        val filteredPopular = popularList.filter {
//            it.drinkName?.contains(query, ignoreCase = true) == true
//        }
//
//        popularAdapter.updateData(filteredPopular)
//    }

    private fun initBottomMenu() {
        binding.cartBtn.setOnClickListener{
            startActivity(Intent(this,CartActivity::class.java))
        }
    }
//
    private fun initBanner() {
        binding.progressBarBanner.visibility = View.VISIBLE
        viewModel.loadBanner().observeForever {
            Glide.with(this@MainActivity)
                .load(it[0].url)
                .into(binding.banner)
            binding.progressBarBanner.visibility = View.GONE
        }
        viewModel.loadBanner()
    }
//
    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.loadCategory().observeForever {
            binding.recyclerViewCat.layoutManager =
                LinearLayoutManager(
                    this@MainActivity, LinearLayoutManager.HORIZONTAL, false
                )
            binding.recyclerViewCat.adapter = CategoryAdapter(it)
            binding.progressBarCategory.visibility = View.GONE
        }
        viewModel.loadCategory()
    }

    private fun initPopular() {
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.loadPopular().observe(this) { populars ->
            populars?.let {
                popularList = it
                popularAdapter = PopularAdapter(it.toMutableList())
                binding.recyclerViewPopular.layoutManager = GridLayoutManager(this, 2)
                binding.recyclerViewPopular.adapter = popularAdapter
            }
            binding.progressBarPopular.visibility = View.GONE
        }
    }
}