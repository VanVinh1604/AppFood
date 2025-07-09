// File: Helper/MomoSignature.kt
package com.example.project1762.Helper

import android.util.Log
import org.json.JSONObject
import java.security.SignatureException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object MomoSignature {
    private const val HMAC_SHA256 = "HmacSHA256"

    fun generate(data: JSONObject, secretKey: String): String {
        val rawData = listOf(
            "accessKey=${data.getString("accessKey")}",
            "amount=${data.getString("amount")}",
            "extraData=${data.getString("extraData")}", // ✅ Sửa lại chỗ này
            "ipnUrl=${data.getString("ipnUrl")}",
            "orderId=${data.getString("orderId")}",
            "orderInfo=${data.getString("orderInfo")}",
            "partnerCode=${data.getString("partnerCode")}",
            "redirectUrl=${data.getString("redirectUrl")}",
            "requestId=${data.getString("requestId")}",
            "requestType=${data.getString("requestType")}"
        ).joinToString("&")
        Log.d("MomoSignature", "RawData: $rawData")
        return hmacSHA256(secretKey, rawData)
    }

    private fun hmacSHA256(key: String, data: String): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(), HMAC_SHA256)
        val mac = Mac.getInstance(HMAC_SHA256)
        mac.init(secretKeySpec)
        val hashBytes = mac.doFinal(data.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
