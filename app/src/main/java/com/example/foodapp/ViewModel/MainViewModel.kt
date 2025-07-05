package com.example.foodapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodapp.Domain.BannerModel
import com.example.foodapp.Domain.CategoryModel
import com.example.foodapp.Domain.CommentModel
import com.example.foodapp.Domain.CustomerModel
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.Domain.OrderDetails
import com.example.foodapp.Domain.VouchersModel
import com.example.foodapp.Repository.CommentRepository
import com.example.foodapp.Repository.MainRepository
import com.example.foodapp.Repository.OrdersRepository
import com.example.foodapp.Repository.PaymentRepository
import com.example.foodapp.Repository.VoucherRepository

class MainViewModel:ViewModel() {
    private val repository = MainRepository()
    private val commentrepository = CommentRepository()
    private val paymentRepository = PaymentRepository()
    private val ordersRepository = OrdersRepository()
    private val voucherRepo = VoucherRepository()



    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        return repository.loadBanner()
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        return repository.loadCategory()
    }

    fun loadPopular(): LiveData<MutableList<ItemsModel>> {
        return repository.loadPopular()
    }

    fun loadItems(categoryId: String): LiveData<MutableList<ItemsModel>> {
        return repository.loadItemCategory(categoryId)
    }

    fun loadComment(drinkId: String): LiveData<MutableList<CommentModel>> {
        return commentrepository.loadComment(drinkId)
    }

    fun getUserInfo(): LiveData<CustomerModel> {
        return paymentRepository.fetchUserInfo()
    }

    fun saveOrder(order: OrderDetails, onResult: (Boolean) -> Unit) {
        paymentRepository.saveOrder(order, onResult)
    }

    fun getOrderHistory(): LiveData<List<OrderDetails>> {
        return ordersRepository.getOrders()
    }


    private val _voucherLiveData = MutableLiveData<Pair< VouchersModel?, String?>>()
    val voucherLiveData: LiveData<Pair<VouchersModel?, String?>>
        get() = _voucherLiveData


    fun decreaseVoucherUsage(code: String) {
        paymentRepository.decreaseVoucherUsage(code)
    }

    private val _userUsedVoucherLiveData = MutableLiveData<Boolean>()
    val userUsedVoucherLiveData: LiveData<Boolean> get() = _userUsedVoucherLiveData

    fun checkUserUsedVoucher(code: String, userId: String) {
        voucherRepo.checkIfUserUsedVoucher(code, userId) {
            _userUsedVoucherLiveData.value = it
        }
    }

    fun validateAndCheckVoucher(code: String, userId: String) {
        voucherRepo.checkVoucher(code) { voucher, error ->
            if (voucher != null) {
                voucherRepo.checkIfUserUsedVoucher(code, userId) { alreadyUsed ->
                    if (alreadyUsed) {
                        _voucherLiveData.postValue(Pair(null, "You have already used this code"))
                    } else {
                        _voucherLiveData.postValue(Pair(voucher, null))
                    }
                }
            } else {
                _voucherLiveData.postValue(Pair(null, error))
            }
        }
    }

    fun markVoucherUsed(code: String, userId: String) {
        voucherRepo.markVoucherUsed(code, userId)
    }

    fun checkUnfinishedOrders(callback: (Boolean) -> Unit) {
        val repo = OrdersRepository()
        repo.hasUnfinishedOrders(callback)
    }



}