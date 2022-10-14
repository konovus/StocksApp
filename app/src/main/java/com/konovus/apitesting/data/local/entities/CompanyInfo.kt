package com.konovus.apitesting.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import java.math.BigInteger
import kotlin.math.ln
import kotlin.math.pow

@Entity
@Parcelize
data class CompanyInfo(
    @PrimaryKey val id: Int? = null,
    @field:Json(name = "Symbol") val symbol: String = "",
    @field:Json(name = "Description") val description: String = "",
    @field:Json(name = "Name") val name: String = "",
    @field:Json(name = "Country") val country: String = "",
    @field:Json(name = "Industry") val industry: String = "",
    @field:Json(name = "Exchange") val exchange: String = "",
    @field:Json(name = "MarketCapitalization") val marketCap: String = "",
    @field:Json(name = "DividendYield") val dividendYield: String = "",
    @field:Json(name = "PERatio") val peRatio: String = "",
    val isFavorite: Boolean = false
): Parcelable {

    fun toStock(): Stock {
        return Stock(
            name = name,
            symbol = symbol,
            exchange = exchange,
            isFavorite = isFavorite
        )
    }
}
