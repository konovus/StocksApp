package com.konovus.apitesting.data.api

import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.data.remote.responses.CompanyOverviewResponse
import com.konovus.apitesting.data.remote.responses.QuoteResponse
import com.konovus.apitesting.util.Constants.API_KEY2
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AlphaVantageApi {

    @GET("query?function=GLOBAL_QUOTE")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("apikey") apikey: String = API_KEY2
    ): Response<QuoteResponse>

    @GET("/query?function=LISTING_STATUS")
    suspend fun getListings(
        @Query("apikey") apikey: String = API_KEY2
    ): ResponseBody

    @GET("/query?interval=60min&datatype=csv")
    suspend fun getIntradayInfo(
        @Query("adjusted") adjusted: Boolean = false,
        @Query("function") function: String = "TIME_SERIES_INTRADAY",
        @Query("symbol") symbol: String,
        @Query("apikey") apikey: String = API_KEY2
    ): ResponseBody

    @GET("/query?function=OVERVIEW")
    suspend fun getCompanyInfo(
        @Query("symbol") symbol: String,
        @Query("apikey") apikey: String = API_KEY2
    ): Response<CompanyOverviewResponse>

}