package com.konovus.apitesting.data.remote.responses

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
}