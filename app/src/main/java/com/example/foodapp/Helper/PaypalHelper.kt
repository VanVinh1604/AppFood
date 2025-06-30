// File: Helper/PaypalHelper.kt
package com.example.project1762.Helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.webkit.URLUtil
import androidx.browser.customtabs.CustomTabsIntent
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object PaypalHelper {
    private const val CLIENT_ID = "Af8Hpkze7EW9TRoOG4wFiutRBWY9nGQc7JcLOgogqUoWj90Kgo_EddhDOK6OeXnywZylZZRLbXPNGjXO"
    private const val SECRET = "EB0FF8hPeWm3xxigv8CMPcTTVdmMYK1tz-4S97e73zFHNuc0paJXac0vfVaupZ13q_FmjeXtlvEkJGLm"
    private const val BASE_URL = "https://api-m.sandbox.paypal.com"

    private var accessToken: String? = null

    fun getAccessToken(callback: (String?) -> Unit) {
        val client = OkHttpClient()

        val credentials = Base64.encodeToString("$CLIENT_ID:$SECRET".toByteArray(), Base64.NO_WRAP)

        val request = Request.Builder()
            .url("$BASE_URL/v1/oauth2/token")
            .addHeader("Authorization", "Basic $credentials")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .post("grant_type=client_credentials".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val token = JSONObject(it).optString("access_token")
                    accessToken = token
                    callback(token)
                } ?: callback(null)
            }
        })
    }

    fun createOrder(context: Context, amount: String, callback: (String?) -> Unit) {
        if (accessToken == null) {
            getAccessToken { token ->
                if (token != null) createOrder(context, amount, callback)
                else callback(null)
            }
            return
        }

        val json = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", org.json.JSONArray().put(
                JSONObject().apply {
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
                        put("value", amount)
                    })
                }
            ))
            put("application_context", JSONObject().apply {
                put("return_url", "myapp://paypal/success")
                put("cancel_url", "myapp://paypal/cancel")
            })
        }

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("$BASE_URL/v2/checkout/orders")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PayPal", "Lỗi kết nối: ${e.message}", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("PayPal", "Phản hồi: $body")
                if (body != null && response.isSuccessful) {
                    val links = JSONObject(body).getJSONArray("links")
                    for (i in 0 until links.length()) {
                        val obj = links.getJSONObject(i)
                        if (obj.getString("rel") == "approve") {
                            callback(obj.getString("href"))
                            return
                        }
                    }
                } else {
                    Log.e("PayPal", "Tạo đơn hàng thất bại: ${response.code} - ${response.message}")
                    callback(null)
                }
            }
        })
    }


    fun openPaypalCheckout(context: Context, approvalUrl: String) {
        if (URLUtil.isValidUrl(approvalUrl)) {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(approvalUrl))
        }
    }
}
