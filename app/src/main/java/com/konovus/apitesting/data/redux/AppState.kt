package com.konovus.apitesting.data.redux

import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.IntraDayInfo
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.remote.responses.EndOfDayDataResponse
import com.konovus.apitesting.data.remote.responses.QuoteResponse
import com.konovus.apitesting.util.NetworkStatus

data class AppState(
    val portfolio: Portfolio? = null,
    val favoritesStocks: List<Stock> = emptyList(),
    val trendingStocks: List<Stock> = emptyList(),
    val detailsStocks: List<Stock> = emptyList(),
    val quoteList: Map<String, QuoteResponse.GlobalQuote> = emptyMap(),
    val endOfDayDataResponseList: Map<String, EndOfDayDataResponse> = emptyMap(),
    val chartData: Map<String, List<IntraDayInfo>> = emptyMap(),
    val bottomNavSelectedId: Int = R.id.mainFragment,
    val networkStatus: NetworkStatus = NetworkStatus.Available
)
