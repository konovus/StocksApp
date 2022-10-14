package com.konovus.apitesting.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "stocks_table")
data class Stock(
    val name: String = "",
    @PrimaryKey
    val symbol: String = "",
    val price: Double = 0.0,
    val change: Double = 0.0,
    val changeInPercentage: Double = 0.0,
    val changeInPercentageString: String = "",
    val description: String = "",
    val exchange: String = "",
    val industry: String = "",
    val sector: String = "",
    val ceo: String = "",
    val marketcap: String = "",
    val isFavorite: Boolean = false,
    val logo: String = "",
    val priceLastUpdated: Long = 0L
): Parcelable