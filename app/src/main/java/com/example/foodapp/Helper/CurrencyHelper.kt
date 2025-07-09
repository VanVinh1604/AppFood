package com.example.project1762.Helper

object CurrencyHelper {
    private const val EXCHANGE_RATE = 1000  // Tạm giả định: 1 đơn vị = 1000 VND

    fun convertToVnd(amountStr: String): Int {
        val amount = amountStr.replace("$", "").trim().toDoubleOrNull() ?: 0.0
        return (amount * EXCHANGE_RATE).toInt()
    }
}
