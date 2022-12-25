package com.konovus.apitesting.data.api

import com.google.gson.internal.LinkedTreeMap
import com.konovus.apitesting.data.remote.responses.ChartsDataResponse
import com.konovus.apitesting.data.remote.responses.MultipleQuotesResponse
import com.konovus.apitesting.data.remote.responses.StockSummaryResponse
import com.konovus.apitesting.data.remote.responses.TrendingStocksResponse
import retrofit2.Response

class FakeYhFinanceApi: YhFinanceApi {

    override suspend fun fetchMultipleQuotes(
        symbols: String,
        region: String
    ): Response<MultipleQuotesResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getStockSummary(symbol: String): Response<StockSummaryResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchTrendingStocks(): Response<TrendingStocksResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchMultipleChartsData(
        symbols: String,
        interval: String,
        range: String
    ): Response<LinkedTreeMap<String, ChartsDataResponse>> {
        TODO("Not yet implemented")
    }
}