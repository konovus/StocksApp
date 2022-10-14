package com.konovus.apitesting.data.remote.responses

import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.util.withSuffix

data class StockDetailsResponse(
    val address: String,
    val ceo: String,
    val description: String,
    val employees: Int,
    val exchange: String,
    val industry: String,
    val logo: String,
    val marketcap: Double,
    val name: String,
    val sector: String,
    val state: String,
    val symbol: String,
    val url: String
) {
    fun toStock() = Stock(
        name = name ?: "",
        symbol = symbol ?: "",
        description = description.ifEmpty { "None" } ?: "",
        exchange = exchange ?: "",
        logo = logo ?: "",
        industry = industry ?: "",
        sector = sector ?: "",
        ceo = ceo ?: "",
        marketcap = marketcap.toString().withSuffix() ?: ""
    )
}