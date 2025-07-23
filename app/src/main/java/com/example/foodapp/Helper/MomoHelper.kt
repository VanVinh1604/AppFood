package com.example.project1762.Helper

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import okhttp3.*
import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object MomoHelper {

    private const val PARTNER_CODE = "MOMO" // Thường là: "MOMO" hoặc "MOMO0HGO20210608"
    private const val ACCESS_KEY = "F8BBA842ECF85"
    private const val SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz"
    private const val REDIRECT_URL = "myapp://momo/success"
    private const val IPN_URL = "https://webhook.site/your-test-url"
    private const val CREATE_ORDER_URL = "https://test-payment.momo.vn/v2/gateway/api/create"

    fun createMomoOrder(
        context: Context,
        amount: String,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        customerAddress: String,
        customerNote: String,
        voucherCode: String?,           // ✅ thêm
        discountAmount: Double?,
        drinkNames: ArrayList<String>,
        drinkIds: ArrayList<String>,    // ✅ thêm param drinkIds
        drinkImages: ArrayList<String>,
        drinkPrices: ArrayList<String>,
        drinkQuantities: ArrayList<Int>,
        drinkSizes: ArrayList<String>,
        callback: (String?) -> Unit
    ) {
        val orderId = System.currentTimeMillis().toString()
        val requestId = System.currentTimeMillis().toString()
        val orderInfo = "Thanh toán đơn hàng FoodApp"
        val amountVnd = CurrencyHelper.convertToVnd(amount)
        val infoJson = JSONObject().apply {
            put("name", customerName)
            put("phone", customerPhone)
            put("address", customerAddress)
            put("note", customerNote)
            put("totalPrice", amount)
            put("voucherCode", voucherCode ?: "")
            put("discountAmount", discountAmount ?: 0.0)
            put("drinkIds", JSONArray(drinkIds))
            put("drinkNames", JSONArray(drinkNames))
            put("drinkImages", JSONArray(drinkImages))
            put("drinkPrices", JSONArray(drinkPrices))
            put("drinkQuantities", JSONArray(drinkQuantities))
            put("drinkSizes", JSONArray(drinkSizes))
        }
        val extraData = Base64.encodeToString(infoJson.toString().toByteArray(), Base64.NO_WRAP)


        val json = JSONObject().apply {
            put("partnerCode", PARTNER_CODE)
            put("accessKey", ACCESS_KEY)
            put("requestId", requestId)
            put("amount", amountVnd)
            put("orderId", orderId)
            put("orderInfo", orderInfo)
            put("redirectUrl", REDIRECT_URL)
            put("ipnUrl", IPN_URL)
            put("extraData", extraData) // ✅ để rỗng
            put("requestType", "captureWallet")
            put("lang", "en")
        }

// ✅ Ghi log dữ liệu trước khi ký
        Log.d("MomoHelper", "Request JSON before signature: $json")

        val signature = MomoSignature.generate(json, SECRET_KEY)
        json.put("signature", signature)

        Log.d("MomoHelper", "Signature: $signature")

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(CREATE_ORDER_URL)
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MomoHelper", "Request failed: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                Log.e("MomoHelper", "HTTP ${response.code} - Body: $bodyStr")

                if (response.isSuccessful && bodyStr != null) {
                    val payUrl = JSONObject(bodyStr).optString("payUrl", null)
                    callback(payUrl)
                } else {
                    callback(null)
                }
            }
        })
    }

    fun openMomoCheckout(context: Context, url: String) {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, Uri.parse(url))
    }
}
