package com.konovus.apitesting.data.remote.responses

import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.util.toNDecimals

data class MostActivesResponse(
        val change: Double,
        val change_percentage: String,
        val company_name: String,
        val price: String,
        val symbol: String
    ) {
        fun toStock() = Stock(
                name = company_name,
                symbol = symbol,
                change = change.toNDecimals(2),
                changeInPercentageString = change_percentage.toDouble().toNDecimals(2).toString(),
                price = price.toDouble().toNDecimals(2)
            )

    }




