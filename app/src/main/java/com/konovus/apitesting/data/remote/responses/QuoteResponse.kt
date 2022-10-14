package com.konovus.apitesting.data.remote.responses

import com.google.gson.annotations.SerializedName
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.util.toNDecimals
import com.konovus.apitesting.util.withSuffix
import com.squareup.moshi.Json

data class QuoteResponse(
    @SerializedName(value = "Global Quote") val globalQuote: GlobalQuote?
) {
     data class GlobalQuote(
        @SerializedName(value = "01. symbol") val symbol: String,
        @SerializedName(value = "02. open") val open: String,
        @SerializedName(value = "03. high") val high: String,
        @SerializedName(value = "04. low") val low: String,
        @SerializedName(value = "05. price") val price: String,
        @SerializedName(value = "06. volume") val volume: String,
        @SerializedName(value = "07. latest trading day") val latestTradingDay: String,
        @SerializedName(value = "08. previous close") val previousClose: String,
        @SerializedName(value = "09. change") val change: String,
        @SerializedName(value = "10. change percent") val changePercent: String,
        val lastUpdatedTime: Long = System.currentTimeMillis()
    ) {
        fun volume() = volume.withSuffix()
        fun price() = price.toDouble().toNDecimals(2).toString()
        fun change() = change.toDouble().toNDecimals(2)
        fun changePercent() = changePercent.substring(0, changePercent.length - 1).toDouble().toNDecimals(2)

    }


}