package com.konovus.apitesting.data.remote.responses

import com.konovus.apitesting.data.local.entities.Stock

data class StockLastQuoteResponse(
    val asize: Int,
    val ask: Double,
    val bid: Double,
    val bsize: Int,
    val symbol: String,
    val timestamp: Long
) {
    fun toStock() = Stock(
        symbol = symbol,
        price = ask,
        lastUpdatedTime = System.currentTimeMillis()
    )
}