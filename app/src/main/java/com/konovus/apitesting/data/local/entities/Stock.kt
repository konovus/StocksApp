package com.konovus.apitesting.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "stocks_table",
    indices = [Index(value = ["symbol"], unique = true)])
data class Stock(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val symbol: String = "",
    val price: Double = 0.0,
    val change: Double = 0.0,
    val changePercent: Double = 0.0,
    val chartChange: ChartChange? = null,
    val quoteType: String = "",
    val isFavorite: Boolean = false,
    val chartOCHLStats: ChartOCHLStats? = null,
    val descriptionStats: DescriptionStats? = null,
    val lastUpdatedTime: Long = 0L
): Parcelable {

    @Parcelize
    data class ChartChange(
        val change: Double = 0.0,
        val changePercent: Double = 0.0,
    ): Parcelable

    @Parcelize
    data class ChartOCHLStats(
        val open: String,
        val prevClose: String,
        val high: String,
        val low: String,
        val volume: String
    ): Parcelable

    @Parcelize
    data class DescriptionStats(
        val exchange: String,
        val industry: String,
        val sector: String,
        val employees: Int,
        val marketCap: String,
        val description: String
    ): Parcelable
}