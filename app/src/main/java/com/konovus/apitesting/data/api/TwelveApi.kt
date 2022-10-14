package com.konovus.apitesting.data.api

import com.google.gson.internal.LinkedTreeMap
import com.konovus.apitesting.data.remote.responses.PriceResponse
import com.konovus.apitesting.util.Constants.TWELVE_API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TwelveApi {

    @GET("price")
    suspend fun getPricesForStocks(
        @Query("symbol") symbol: String,
        @Query("apikey") apikey: String = TWELVE_API_KEY
    ): Response<LinkedTreeMap<String, PriceResponse>>

    @GET("price")
    suspend fun getPriceForStock(
        @Query("symbol") symbol: String,
        @Query("apikey") apikey: String = TWELVE_API_KEY
    ): Response<LinkedTreeMap<String, String>>
}