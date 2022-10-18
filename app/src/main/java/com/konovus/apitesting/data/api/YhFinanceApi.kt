package com.konovus.apitesting.data.api

import com.konovus.apitesting.data.remote.responses.MultipleQuotesResponse
import com.konovus.apitesting.util.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface YhFinanceApi {

    @Headers("X-RapidAPI-Key: ${Constants.API_KEY_YH_FINANCE}",
            "X-RapidAPI-Host: yh-finance.p.rapidapi.com")
    @GET("market/v2/get-quotes")
    suspend fun getMultipleQuotes(
        @Query("symbols") symbols: String,
        @Query("symbols") region: String = "US",
    ): Response<MultipleQuotesResponse>

}