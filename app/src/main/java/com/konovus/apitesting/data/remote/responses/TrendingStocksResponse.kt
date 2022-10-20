package com.konovus.apitesting.data.remote.responses

import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.util.toNDecimals

data class TrendingStocksResponse(
    val finance: Finance
) {
    data class Finance(
        val error: Any,
        val result: List<Result>
    ) {
        data class Result(
            val count: Int,
            val jobTimestamp: Long,
            val quotes: List<Quote>,
            val startInterval: Long
        ) {
            data class Quote(
                val esgPopulated: Boolean,
                val exchange: String,
                val exchangeDataDelayedBy: Int,
                val exchangeTimezoneName: String,
                val exchangeTimezoneShortName: String,
                val fullExchangeName: String,
                val gmtOffSetMilliseconds: Int,
                val language: String,
                val longName: String,
                val market: String,
                val marketState: String,
                val priceHint: Int,
                val quoteSourceName: String,
                val quoteType: String,
                val region: String,
                val regularMarketChange: Double,
                val regularMarketChangePercent: Double,
                val regularMarketPreviousClose: Double,
                val regularMarketPrice: Double,
                val regularMarketTime: Int,
                val shortName: String,
                val sourceInterval: Int,
                val symbol: String,
                val tradeable: Boolean,
                val triggerable: Boolean
            ) {
                fun toStock() = Stock(
                    name = longName,
                    symbol = symbol,
                    price = regularMarketPrice,
                    change = regularMarketChange,
                    changePercent = regularMarketChangePercent.toNDecimals(2)
                )
            }
        }
    }




}