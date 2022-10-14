package com.konovus.apitesting.data.local.entities

import android.os.Parcelable
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.toNDecimals
import kotlinx.parcelize.Parcelize

@Entity(tableName = "portfolios_table")
@Parcelize
data class Portfolio(
    @PrimaryKey val id: Int? = null,
    val name: String = "Default",
    val totalBalance: Double = 0.0,
    val change: Double = 0.0,
    val changeInPercentage: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val lastUpdatedTime: Long = 0L
): Parcelable {

    val stocksToShareAmount: Map<String, Double>
        get() = transactions.map {
            it.symbol to if (it.orderType == OrderType.Buy) (it.amount / it.price).toNDecimals(2)
            else -(it.amount / it.price).toNDecimals(2)
        }.groupBy { it.first }.map { it.key to it.value.map { it.second }.sumOf { it }.toNDecimals(2) }
            .toMap().filter { it.value > 0 }

    fun stocksToShareAmount(): Map<String, Double> {
        val stocksToShareAmount =
            transactions.
            map {
            it.symbol to if (it.orderType == OrderType.Buy) (it.amount / it.price).toNDecimals(2)
            else -(it.amount / it.price).toNDecimals(2)
        }.groupBy { it.first }.map { it.key to it.value.map { it.second }.sumOf { it }.toNDecimals(2) }.toMap()
        stocksToShareAmount.map { Pair(it.key, it.value) }.toMutableList().removeIf{ it.second == 0.0 }
        return stocksToShareAmount
    }
}