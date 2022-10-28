package com.konovus.apitesting.data.api

import com.google.gson.internal.LinkedTreeMap
import com.konovus.apitesting.data.remote.responses.ChartsData
import com.konovus.apitesting.data.remote.responses.MultipleQuotesResponse
import com.konovus.apitesting.data.remote.responses.StockSummaryResponse
import com.konovus.apitesting.data.remote.responses.TrendingStocksResponse
import com.konovus.apitesting.BuildConfig.API_KEY_YH_FINANCE
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface YhFinanceApi {

    @Headers("X-RapidAPI-Key: $API_KEY_YH_FINANCE",
            "X-RapidAPI-Host: yh-finance.p.rapidapi.com")
    @GET("market/v2/get-quotes")
    suspend fun getMultipleQuotes(
        @Query("symbols") symbols: String,
        @Query("symbols") region: String = "US",
    ): Response<MultipleQuotesResponse>

    @Headers("X-RapidAPI-Key: $API_KEY_YH_FINANCE",
            "X-RapidAPI-Host: yh-finance.p.rapidapi.com")
    @GET("stock/v2/get-summary")
    suspend fun getStockSummary(
        @Query("symbol") symbol: String,
    ): Response<StockSummaryResponse>

    @Headers("X-RapidAPI-Key: $API_KEY_YH_FINANCE",
            "X-RapidAPI-Host: yh-finance.p.rapidapi.com")
    @GET("market/get-trending-tickers")
    suspend fun getTrendingStocks(): Response<TrendingStocksResponse>


    @Headers("X-RapidAPI-Key: $API_KEY_YH_FINANCE",
            "X-RapidAPI-Host: yh-finance.p.rapidapi.com")
    @GET("market/get-spark")
    suspend fun getMultipleChartsData(
        @Query("symbols") symbols: String,
        @Query("interval") interval: String = "60m",
        @Query("range") range: String = "1d"
    ): Response<LinkedTreeMap<String, ChartsData>>


}