package com.example.foodapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.foodapp.Domain.BannerModel
import com.example.foodapp.Domain.CategoryModel
import com.example.foodapp.Domain.CommentModel
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.Repository.CommentRepository
import com.example.foodapp.Repository.MainRepository

class MainViewModel:ViewModel() {
    private val repository=MainRepository()
    private val commentrepository= CommentRepository()

    fun loadBanner():LiveData<MutableList<BannerModel>>{
        return repository.loadBanner()
    }
    fun loadCategory():LiveData<MutableList<CategoryModel>>{
        return repository.loadCategory()
    }
    fun loadPopular():LiveData<MutableList<ItemsModel>>{
        return repository.loadPopular()
    }
    fun loadItems(categoryId:String):LiveData<MutableList<ItemsModel>>{
        return repository.loadItemCategory(categoryId)
    }
    fun loadComment(drinkId: String): LiveData<MutableList<CommentModel>>{
        return commentrepository.loadComment(drinkId)
    }
}