package com.konovus.apitesting.data.local.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Transaction(
    val price: Double,
    val symbol: String,
    val amount: Double,
    val dateTime: Long,
    val orderType: OrderType
): Parcelable {
    fun getFormattedDate(): String {
        val date = Date(dateTime)
        val format = SimpleDateFormat("dd-MM-yy")
        return format.format(date)
    }
}

enum class OrderType {
    Buy, Sell
}
