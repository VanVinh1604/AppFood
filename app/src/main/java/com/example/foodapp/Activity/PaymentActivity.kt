package com.example.foodapp.Activity

import PaymentViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.Domain.OrderDetails
import com.example.foodapp.R
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.databinding.ActivityPaymentBinding
import com.example.project1762.Helper.ManagmentCart
import com.example.project1762.Helper.MomoHelper
import com.example.project1762.Helper.PaypalHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Adapter.VoucherAdapter
import com.example.foodapp.Domain.VouchersModel
import org.json.JSONObject
import com.google.firebase.database.FirebaseDatabase



class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private val paymentViewModel: MainViewModel by viewModels()
    private var isPaypalApproved = false
    private var isMomoApproved = false
    private var appliedVoucherCode: String? = null
    private var discountAmount: Double = 0.0
    private var finalTotalPrice: String? = null
    private val stateViewModel: PaymentViewModel by viewModels()

    // 🔧 Lưu trữ dữ liệu để tránh mất khi Activity recreate
    private var savedName: String = ""
    private var savedAddress: String = ""
    private var savedPhone: String = ""
    private var savedNote: String = ""
    private val handler = Handler(Looper.getMainLooper())
    private var paymentRunnable: Runnable? = null
    private var drinkNames: ArrayList<String> = arrayListOf()
    private var drinkImages: ArrayList<String> = arrayListOf()
    private var drinkPrices: ArrayList<String> = arrayListOf()
    private var drinkQuantities: ArrayList<Int> = arrayListOf()
    private var drinkSizes: ArrayList<String> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerVoucher)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

// Gọi load data từ Firebase
        paymentViewModel.fetchAllActiveVouchers()

// Observe dữ liệu vouchers
        paymentViewModel.allVouchers.observe(this) { voucherList ->
            val adapter = VoucherAdapter(voucherList) { selectedVoucher ->
                binding.editTextDiscount.setText(selectedVoucher.code)
                Toast.makeText(this, "Đã chọn mã: ${selectedVoucher.code}", Toast.LENGTH_SHORT).show()
            }
            recyclerView.adapter = adapter
        }

        // Khôi phục dữ liệu nếu có
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }


        val originalPrice = intent.getStringExtra("totalPrice") ?: "0.0"
        finalTotalPrice = originalPrice
        binding.totalInput.setText("$${originalPrice}")

        observeUserInfo()
        binding.backButton.setOnClickListener { finish() }
        initVoucherApply()
        observePaymentData()

        drinkNames = intent.getStringArrayListExtra("drinkNames") ?: arrayListOf()
        drinkImages = intent.getStringArrayListExtra("drinkImages") ?: arrayListOf()
        drinkPrices = intent.getStringArrayListExtra("drinkPrices") ?: arrayListOf()
        drinkQuantities = intent.getIntegerArrayListExtra("drinkQuantities") ?: arrayListOf()
        drinkSizes = intent.getStringArrayListExtra("drinkSizes") ?: arrayListOf()

        binding.ocdBtn.setOnClickListener {
            // 💾 Lưu dữ liệu trước khi thanh toán
            saveCurrentData()

            val method = when (binding.paymentContainer.checkedRadioButtonId) {
                R.id.radioCOD -> "COD"
                R.id.radioMomo -> "Momo"
                R.id.radioPaypal -> "PayPal"
                else -> null
            }

            if (method == null) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = finalTotalPrice ?: intent.getStringExtra("totalPrice") ?: "0.00"

            when (method) {
                "PayPal" -> startPaypalPayment(amount)
                "Momo" -> startMomoPayment(amount)
                else -> placeOrder(method)
            }
        }
    }

    // 💾 Lưu dữ liệu hiện tại
    private fun saveCurrentData() {
        val name = binding.nameInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val note = binding.noteInput.text.toString().trim()

        savedName = name
        savedAddress = address
        savedPhone = phone
        savedNote = note

        stateViewModel.savePaymentData(
            name = name,
            address = address,
            phone = phone,
            note = note,
            totalPrice = finalTotalPrice,
            voucherCode = appliedVoucherCode,
            discountAmount = discountAmount,
            isPaypalApproved = isPaypalApproved,
            isMomoApproved = isMomoApproved
        )
    }


    // 📋 Khôi phục dữ liệu
    private fun restoreData() {
        if (savedName.isNotEmpty()) binding.nameInput.setText(savedName)
        if (savedAddress.isNotEmpty()) binding.addressInput.setText(savedAddress)
        if (savedPhone.isNotEmpty()) binding.phoneInput.setText(savedPhone)
        if (savedNote.isNotEmpty()) binding.noteInput.setText(savedNote)
    }

    // 💾 Lưu trạng thái khi Activity bị destroy
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveCurrentData()
        outState.putString("savedName", savedName)
        outState.putString("savedAddress", savedAddress)
        outState.putString("savedPhone", savedPhone)
        outState.putString("savedNote", savedNote)
        outState.putString("appliedVoucherCode", appliedVoucherCode)
        outState.putDouble("discountAmount", discountAmount)
        outState.putString("finalTotalPrice", finalTotalPrice)
        outState.putBoolean("isMomoApproved", isMomoApproved)
        outState.putBoolean("isPaypalApproved", isPaypalApproved)
        outState.putStringArrayList("drinkNames", drinkNames)
        outState.putStringArrayList("drinkImages", drinkImages)
        outState.putStringArrayList("drinkPrices", drinkPrices)
        outState.putIntegerArrayList("drinkQuantities", drinkQuantities)
        outState.putStringArrayList("drinkSizes", drinkSizes)
    }

    // 📋 Khôi phục trạng thái
    private fun restoreInstanceState(savedInstanceState: Bundle) {
        savedName = savedInstanceState.getString("savedName", "")
        savedAddress = savedInstanceState.getString("savedAddress", "")
        savedPhone = savedInstanceState.getString("savedPhone", "")
        savedNote = savedInstanceState.getString("savedNote", "")
        appliedVoucherCode = savedInstanceState.getString("appliedVoucherCode")
        discountAmount = savedInstanceState.getDouble("discountAmount", 0.0)
        finalTotalPrice = savedInstanceState.getString("finalTotalPrice")
        isMomoApproved = savedInstanceState.getBoolean("isMomoApproved", false)
        isPaypalApproved = savedInstanceState.getBoolean("isPaypalApproved", false)
        drinkNames = savedInstanceState.getStringArrayList("drinkNames") ?: arrayListOf()
        drinkImages = savedInstanceState.getStringArrayList("drinkImages") ?: arrayListOf()
        drinkPrices = savedInstanceState.getStringArrayList("drinkPrices") ?: arrayListOf()
        drinkQuantities = savedInstanceState.getIntegerArrayList("drinkQuantities") ?: arrayListOf()
        drinkSizes = savedInstanceState.getStringArrayList("drinkSizes") ?: arrayListOf()

        // Khôi phục UI
        restoreData()
    }

    override fun onResume() {
        super.onResume()
        restoreData() // Khôi phục dữ liệu khi quay lại
        handlePaymentResult(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // ⚠️ Quan trọng: cập nhật intent mới
        handlePaymentResult(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 🧹 Dọn dẹp Handler để tránh memory leak
        paymentRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun handlePaymentResult(intent: Intent?) {
        val uri = intent?.data ?: return

        if (uri.scheme == "myapp") {
            when (uri.host) {
                "paypal" -> {
                    when (uri.path) {
                        "/success" -> {
                            if (!isPaypalApproved) {
                                isPaypalApproved = true
                                Toast.makeText(this, "Thanh toán PayPal thành công", Toast.LENGTH_SHORT).show()
                                placeOrder("PayPal")
                            }
                        }
                        "/cancel" -> {
                            Toast.makeText(this, "Bạn đã huỷ thanh toán PayPal", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                "momo" -> {
                    when (uri.path) {
                        "/success" -> {
                            if (!isMomoApproved) {
                                isMomoApproved = true
                                Toast.makeText(this, "Thanh toán MoMo thành công!", Toast.LENGTH_SHORT).show()

                                // ✅ GIẢI MÃ extraData từ URL
                                val extraDataBase64 = uri.getQueryParameter("extraData")
                                extraDataBase64?.let {
                                    try {
                                        val decodedBytes = android.util.Base64.decode(it, android.util.Base64.NO_WRAP)
                                        val json = JSONObject(String(decodedBytes))

                                        savedName = json.optString("name", savedName)
                                        savedPhone = json.optString("phone", savedPhone)
                                        savedAddress = json.optString("address", savedAddress)
                                        savedNote = json.optString("note", savedNote)
                                        finalTotalPrice = json.optString("totalPrice", finalTotalPrice)

                                        // 🔧 Phần còn thiếu cần thêm:
                                        appliedVoucherCode = json.optString("voucherCode", null)
                                        discountAmount = json.optDouble("discountAmount", 0.0)

                                        val drinkNamesArray = json.optJSONArray("drinkNames")
                                        val drinkImagesArray = json.optJSONArray("drinkImages")
                                        val drinkPricesArray = json.optJSONArray("drinkPrices")
                                        val drinkQuantitiesArray = json.optJSONArray("drinkQuantities")
                                        val drinkSizesArray = json.optJSONArray("drinkSizes")

                                        drinkNames.clear()
                                        drinkImages.clear()
                                        drinkPrices.clear()
                                        drinkQuantities.clear()
                                        drinkSizes.clear()

                                        for (i in 0 until (drinkNamesArray?.length() ?: 0)) {
                                            drinkNames.add(drinkNamesArray?.optString(i) ?: "")
                                        }
                                        for (i in 0 until (drinkImagesArray?.length() ?: 0)) {
                                            drinkImages.add(drinkImagesArray?.optString(i) ?: "")
                                        }
                                        for (i in 0 until (drinkPricesArray?.length() ?: 0)) {
                                            drinkPrices.add(drinkPricesArray?.optString(i) ?: "")
                                        }
                                        for (i in 0 until (drinkQuantitiesArray?.length() ?: 0)) {
                                            drinkQuantities.add(drinkQuantitiesArray?.optInt(i) ?: 0)
                                        }
                                        for (i in 0 until (drinkSizesArray?.length() ?: 0)) {
                                            drinkSizes.add(drinkSizesArray?.optString(i) ?: "")
                                        }

                                        restoreData() // cập nhật lại UI
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                // 🔄 Đợi 1 chút để dữ liệu trên UI load lại rồi mới gọi placeOrder
                                paymentRunnable = Runnable {
                                    if (!isFinishing && !isDestroyed) {
                                        placeOrder("Momo")
                                    }
                                }
                                handler.postDelayed(paymentRunnable!!, 2000)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun placeOrder(paymentMethod: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔧 Sử dụng dữ liệu đã lưu thay vì đọc từ UI
        val name = if (savedName.isNotEmpty()) savedName else binding.nameInput.text.toString().trim()
        val address = if (savedAddress.isNotEmpty()) savedAddress else binding.addressInput.text.toString().trim()
        val phone = if (savedPhone.isNotEmpty()) savedPhone else binding.phoneInput.text.toString().trim()
        val note = if (savedNote.isNotEmpty()) savedNote else binding.noteInput.text.toString().trim()

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin cá nhân", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy drinkIds từ intent
        val drinkIds = intent.getStringArrayListExtra("drinkIds") ?: arrayListOf()

        val order = OrderDetails(
            customerId = uid,
            customerName = name,
            address = address,
            phoneNumber = phone,
            note = note,
            totalPrice = finalTotalPrice,
            drinkNames = drinkNames,
            drinkImages = drinkImages,
            drinkPrices = drinkPrices,
            drinkQuantities = drinkQuantities,
            drinkSizes = drinkSizes,
            paymentStatus = paymentMethod,
            currentTime = System.currentTimeMillis(),
            voucherCode = appliedVoucherCode,
            discountAmount = if (discountAmount > 0.0) "%.2f".format(discountAmount) else null,
            paymentReceived = paymentMethod == "Momo" || paymentMethod == "PayPal"
        )

        paymentViewModel.saveOrder(order) { success, savedOrder ->
            if (success && savedOrder != null) {
                appliedVoucherCode?.let { code ->
                    paymentViewModel.decreaseVoucherUsage(code)
                    paymentViewModel.markVoucherUsed(code, uid)
                }

                ManagmentCart(this).clearCart()
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()

                // Quay lại CartActivity trước, và xóa hết stack
                val cartIntent = Intent(this, CartActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(cartIntent)

// Sau đó mở OrderDetailsActivity (trên Cart)
                val orderIntent = Intent(this, OrderDetailsActivity::class.java).apply {
                    putExtra("order", savedOrder)
                }
                startActivity(orderIntent)
                stateViewModel.savePaymentData(
                    name = "",
                    address = "",
                    phone = "",
                    note = "",
                    totalPrice = null,
                    voucherCode = null,
                    discountAmount = 0.0,
                    isPaypalApproved = false,
                    isMomoApproved = false
                )
            } else {
                Toast.makeText(this, "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Các hàm khác giữ nguyên...
    private fun observeUserInfo() {
        paymentViewModel.getUserInfo().observe(this) { user ->
            if (user != null) {
                user.nameCustomer?.let {
                    if (savedName.isEmpty()) {
                        binding.nameInput.setText(it)
                        savedName = it
                    }
                }
                user.addressCustomer?.let {
                    if (savedAddress.isEmpty()) {
                        binding.addressInput.setText(it)
                        savedAddress = it
                    }
                }
                user.phoneNumberCustomer?.let {
                    if (savedPhone.isEmpty()) {
                        binding.phoneInput.setText(it)
                        savedPhone = it
                    }
                }
            }
        }
    }

    private fun startMomoPayment(amount: String) {
        val cleanedAmount = amount.replace("$", "").trim().toDoubleOrNull() ?: 0.0

        if (cleanedAmount <= 0.0) {
            Toast.makeText(this, "Số tiền không hợp lệ để thanh toán MoMo", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "noemail@example.com"

        MomoHelper.createMomoOrder(
            context = this,
            amount = amount,
            customerName = savedName.ifEmpty { binding.nameInput.text.toString() },
            customerEmail = email,
            customerPhone = savedPhone.ifEmpty { binding.phoneInput.text.toString() },
            customerAddress = savedAddress.ifEmpty { binding.addressInput.text.toString() },
            customerNote = savedNote.ifEmpty { binding.noteInput.text.toString() },
            voucherCode = appliedVoucherCode,                    // ✅ truyền đúng
            discountAmount = discountAmount,
            drinkNames = drinkNames,
            drinkImages = drinkImages,
            drinkPrices = drinkPrices,
            drinkQuantities = drinkQuantities,
            drinkSizes = drinkSizes // ✅ thêm dòng này
        ) { payUrl ->
            runOnUiThread {
                if (payUrl != null) {
                    MomoHelper.openMomoCheckout(this, payUrl)
                } else {
                    Toast.makeText(this, "Tạo đơn MoMo thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun observePaymentData() {
        stateViewModel.paymentData.observe(this) { data ->
            if (data.name.isNotEmpty()) binding.nameInput.setText(data.name)
            if (data.address.isNotEmpty()) binding.addressInput.setText(data.address)
            if (data.phone.isNotEmpty()) binding.phoneInput.setText(data.phone)
            if (data.note.isNotEmpty()) binding.noteInput.setText(data.note)

            appliedVoucherCode = data.voucherCode
            discountAmount = data.discountAmount
            finalTotalPrice = data.totalPrice
            isPaypalApproved = data.isPaypalApproved
            isMomoApproved = data.isMomoApproved

            data.totalPrice?.let {
                binding.totalInput.setText("$$it")
            }
        }
    }


    private fun startPaypalPayment(totalAmount: String) {
        PaypalHelper.createOrder(this, totalAmount) { approvalUrl ->
            if (approvalUrl != null) {
                runOnUiThread {
                    PaypalHelper.openPaypalCheckout(this, approvalUrl)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Tạo đơn PayPal thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initVoucherApply() {
        binding.buttonApply.setOnClickListener {
            val code = binding.editTextDiscount.text.toString().trim()
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (code.isNotEmpty() && uid != null) {
                paymentViewModel.validateAndCheckVoucher(code, uid)
            } else {
                Toast.makeText(this, "Không có mã giảm giá nào được áp dụng", Toast.LENGTH_SHORT).show()
            }
        }

        paymentViewModel.voucherLiveData.observe(this) { (voucher, error) ->
            if (voucher != null) {
                val totalStr = intent.getStringExtra("totalPrice") ?: "0"
                val total = totalStr.replace("$", "").toDoubleOrNull() ?: 0.0

                if (total >= voucher.minOrderValue) {
                    val percentDiscount = total * voucher.discountPercent / 100
                    val discount = voucher.maxDiscount?.let { max ->
                        percentDiscount.coerceAtMost(max)
                    } ?: percentDiscount

                    val finalTotal = total - discount

                    appliedVoucherCode = voucher.code
                    discountAmount = discount
                    finalTotalPrice = "%.2f".format(finalTotal)

                    binding.totalInput.setText("$$finalTotalPrice")
                    binding.buttonApply.isEnabled = false

                    Toast.makeText(
                        this,
                        "Áp dụng mã giảm giá thành công! Giảm $${"%.2f".format(discount)}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Đơn hàng chưa đủ điều kiện để áp dụng mã này", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, error ?: "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun markOrderRatedFalse(order: OrderDetails) {
        val orderRef = FirebaseDatabase.getInstance().getReference("Orders")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val names = order.drinkNames ?: return
        val images = order.drinkImages
        val prices = order.drinkPrices
        val quantities = order.drinkQuantities
        val sizes = order.drinkSizes


        // Lấy drinkIds từ Cart
        val managementCart = ManagmentCart(this)
        val cartItems = managementCart.getListCart()
        val drinkIds = cartItems.map { it.drinkId }

        orderRef.child(userId).get().addOnSuccessListener { snapshot ->
            val lastOrderKey = snapshot.children.lastOrNull()?.key
            if (lastOrderKey != null) {
                val orderNode = orderRef.child(userId).child(lastOrderKey)

                // Lưu orderTime vào order
                orderNode.child("orderTime").setValue(System.currentTimeMillis())

                // Lưu status là Delivered
                orderNode.child("status").setValue("Delivered")

                // Lưu từng item
                for (i in names.indices) {
                    val itemData = mapOf(
                        "drinkId" to drinkIds.getOrNull(i),
                        "drinkName" to names[i],
                        "drinkImage" to images?.getOrNull(i),
                        "drinkPrice" to prices?.getOrNull(i)?.toDoubleOrNull(),
                        "drinkQuantity" to quantities?.getOrNull(i),
                        "drinkSize" to sizes?.getOrNull(i),
                        "isReviewed" to false
                    )

                    orderNode.child("items").child("item_$i").setValue(itemData)
                        .addOnSuccessListener {
                            Log.d("PaymentActivity", "Item $i saved successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("PaymentActivity", "Failed to save item $i: ${e.message}")
                        }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("PaymentActivity", "Không thể đọc Orders: ${e.message}")
        }
    }
}