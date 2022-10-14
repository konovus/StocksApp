package com.konovus.apitesting.data.api

import com.konovus.apitesting.data.remote.responses.EndOfDayDataResponse
import com.konovus.apitesting.data.remote.responses.MostActivesResponse
import com.konovus.apitesting.data.remote.responses.StockDetailsResponse
import com.konovus.apitesting.data.remote.responses.StockLastQuoteResponse
import com.konovus.apitesting.util.Constants.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface FinageApi {

    @GET("market-information/us/most-actives")
    suspend fun getMostActive(
        @Query("apikey") apikey: String = API_KEY
    ): Response<List<MostActivesResponse>>

    @GET("detail/stock/{symbol}")
    suspend fun getStockDetails(
        @Path("symbol") symbol: String,
        @Query("apikey") apikey: String = API_KEY
    ): Response<StockDetailsResponse>

    @GET("last/stocks/")
    suspend fun getMultipleStocks(
        @Query("symbols") symbols: String,
        @Query("apikey") apikey: String = API_KEY
    ): Response<List<StockLastQuoteResponse?>>

    @GET("history/stock/open-close")
    suspend fun getEndOfDayData(
        @Query("stock") symbol: String,
        @Query("date") date: String = "2022-07-29",
        @Query("apikey") apikey: String = API_KEY
    ): Response<EndOfDayDataResponse>

}