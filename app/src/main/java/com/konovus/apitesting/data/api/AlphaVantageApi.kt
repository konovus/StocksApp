package com.konovus.apitesting.data.api

import com.konovus.apitesting.BuildConfig.API_KEY_ALPHA_VANTAGE
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query


interface AlphaVantageApi {

    @GET("/query?function=LISTING_STATUS")
    suspend fun getListings(
        @Query("apikey") apikey: String = API_KEY_ALPHA_VANTAGE
    ): ResponseBody

    @GET("/query?interval=60min&datatype=csv")
    suspend fun getIntradayInfo(
        @Query("adjusted") adjusted: Boolean = false,
        @Query("function") function: String = "TIME_SERIES_INTRADAY",
        @Query("symbol") symbol: String,
        @Query("apikey") apikey: String = API_KEY_ALPHA_VANTAGE
    ): ResponseBody

}