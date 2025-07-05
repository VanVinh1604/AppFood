package com.example.foodapp.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.foodapp.R
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.Adapter.ItemsListCategoryAdapter
import com.example.foodapp.Domain.ItemsModel
import androidx.appcompat.widget.SearchView
import com.example.foodapp.Adapter.CategoryAdapter
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.databinding.ActivityItemsListBinding

class ItemsListActivity : AppCompatActivity() {

    lateinit var binding: ActivityItemsListBinding
    private val viewModel = MainViewModel()

    private var id: String = ""
    private var title: String = ""
    private var fullItemList: List<ItemsModel> = listOf()
    private lateinit var itemsAdapter: ItemsListCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityItemsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getBundle()
        initCategoryHorizontal()
        initList()
        initSearch()
    }

    private fun getBundle(){
        id = intent.getStringExtra("categoryId") ?: "all"
        title = intent.getStringExtra("category_Name") ?: ""

        if (title.isEmpty()) {
            Toast.makeText(this, "Thiếu tên danh mục!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.categoryTxt.text = title
        binding.backBtn.setOnClickListener { finish() }
    }


    private fun initList() {
        binding.progressBar.visibility = View.VISIBLE
        itemsAdapter = ItemsListCategoryAdapter(mutableListOf())
        binding.listView.layoutManager = LinearLayoutManager(this)
        binding.listView.adapter = itemsAdapter

        viewModel.loadItems(id).observe(this) { items ->
            fullItemList = items
            itemsAdapter.items.clear()
            itemsAdapter.items.addAll(items)
            itemsAdapter.notifyDataSetChanged()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun initSearch() {
        // Lấy EditText con của SearchView
        val searchEditText = binding.searchView.findViewById<android.widget.EditText>(
            androidx.appcompat.R.id.search_src_text
        )

        // Thiết lập màu
        searchEditText.setTextColor(resources.getColor(R.color.darkBrown))
        searchEditText.setHintTextColor(resources.getColor(R.color.darkBrown))

        // Bấm vào SearchView là focus luôn vào ô nhập
        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false // Mở rộng nếu bị thu gọn
            binding.searchView.requestFocus()
            searchEditText.requestFocus()
        }

        // Tự động focus khi click vào vùng SearchLayout (nếu muốn mở rộng vùng bấm)
        binding.searchLayout.setOnClickListener {
            binding.searchView.isIconified = false
            binding.searchView.requestFocus()
            searchEditText.requestFocus()
        }

        // Bắt sự kiện tìm kiếm
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterItems(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterItems(it) }
                return true
            }
        })

    }


    private fun filterItems(query: String) {
        val filtered = fullItemList.filter {
            it.drinkName?.contains(query, ignoreCase = true) == true
        }
        itemsAdapter.items.clear()
        itemsAdapter.items.addAll(filtered)
        itemsAdapter.notifyDataSetChanged()
    }

    private fun initCategoryHorizontal() {
        binding.categoryList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        viewModel.loadCategory().observe(this) { categories ->

            val allCategory = com.example.foodapp.Domain.CategoryModel(
                categoryId = "all",
                category_Name = "All Products"
            )

            val updatedList = mutableListOf(allCategory)
            updatedList.addAll(categories)

            // Gắn adapter với danh sách mới
            binding.categoryList.adapter = CategoryAdapter(updatedList.toMutableList()) { selected ->
                id = selected.categoryId ?: "all"
                title = selected.category_Name ?: "All Products"
                binding.categoryTxt.text = title
                reloadItemsByCategory(id)
            }
        }
    }


    private fun reloadItemsByCategory(categoryId: String) {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadItems(categoryId).observe(this) { items ->
            fullItemList = items
            itemsAdapter.items.clear()
            itemsAdapter.items.addAll(items)
            itemsAdapter.notifyDataSetChanged()
            binding.progressBar.visibility = View.GONE
        }
    }
}