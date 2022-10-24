package com.konovus.apitesting.data.redux

import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.util.NetworkStatus

data class AppState(
    val portfolio: Portfolio? = null,
    val favorites: List<String> = emptyList(),
    val trendingStocks: List<Stock> = emptyList(),
    val stockList: List<Stock> = emptyList(),
    val chartData: Map<String, List<ChartData>> = emptyMap(),
    val bottomNavSelectedId: Int = R.id.mainFragment,
    val networkStatus: NetworkStatus = NetworkStatus.Available
)
