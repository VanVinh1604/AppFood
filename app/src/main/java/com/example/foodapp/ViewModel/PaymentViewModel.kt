import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class PaymentViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _paymentData = MutableLiveData<PaymentData>()
    val paymentData: LiveData<PaymentData> get() = _paymentData

    data class PaymentData(
        val name: String = "",
        val address: String = "",
        val phone: String = "",
        val note: String = "",
        val totalPrice: String? = null,
        val voucherCode: String? = null,
        val discountAmount: Double = 0.0,
        val isPaypalApproved: Boolean = false,
        val isMomoApproved: Boolean = false
    )

    init {
        // Khôi phục dữ liệu từ SavedStateHandle
        _paymentData.value = PaymentData(
            name = savedStateHandle.get<String>("name") ?: "",
            address = savedStateHandle.get<String>("address") ?: "",
            phone = savedStateHandle.get<String>("phone") ?: "",
            note = savedStateHandle.get<String>("note") ?: "",
            totalPrice = savedStateHandle.get<String>("totalPrice"),
            voucherCode = savedStateHandle.get<String>("voucherCode"),
            discountAmount = savedStateHandle.get<Double>("discountAmount") ?: 0.0,
            isPaypalApproved = savedStateHandle.get<Boolean>("isPaypalApproved") ?: false,
            isMomoApproved = savedStateHandle.get<Boolean>("isMomoApproved") ?: false
        )
    }

    fun savePaymentData(
        name: String,
        address: String,
        phone: String,
        note: String,
        totalPrice: String?,
        voucherCode: String?,
        discountAmount: Double,
        isPaypalApproved: Boolean,
        isMomoApproved: Boolean
    ) {
        _paymentData.value = PaymentData(
            name, address, phone, note, totalPrice, voucherCode, discountAmount, isPaypalApproved, isMomoApproved
        )
        // Lưu vào SavedStateHandle
        savedStateHandle["name"] = name
        savedStateHandle["address"] = address
        savedStateHandle["phone"] = phone
        savedStateHandle["note"] = note
        savedStateHandle["totalPrice"] = totalPrice
        savedStateHandle["voucherCode"] = voucherCode
        savedStateHandle["discountAmount"] = discountAmount
        savedStateHandle["isPaypalApproved"] = isPaypalApproved
        savedStateHandle["isMomoApproved"] = isMomoApproved
    }
}