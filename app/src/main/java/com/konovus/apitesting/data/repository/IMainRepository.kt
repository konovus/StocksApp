package com.konovus.apitesting.data.repository

import com.google.gson.internal.LinkedTreeMap
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.Profile
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.data.remote.responses.ChartsDataResponse
import com.konovus.apitesting.data.remote.responses.MultipleQuotesResponse
import com.konovus.apitesting.data.remote.responses.StockSummaryResponse
import com.konovus.apitesting.data.remote.responses.TrendingStocksResponse
import com.konovus.apitesting.util.Resource
import kotlinx.coroutines.flow.Flow
import retrofit2.Response


interface IMainRepository {
    val favoritesCache: List<Quote>
    val stocksCache: List<Stock>
    val chartDataCache: Map<String, List<ChartData>>
    val portfolioQuotesCache: List<Quote>

    suspend fun updateFavoritesCacheQuote(quote: Quote)

    suspend fun updateFavoritesCache(stocks: List<Quote>): Boolean

    suspend fun updatePortfolioStocksCache(quote: Quote): Boolean

    suspend fun removeFromPortfolioStocksCache(symbol: String)

    suspend fun updateChartDataCache(key: String, value: List<ChartData>)

    suspend fun updateStocksCache(stock: Stock): Boolean

    suspend fun <T> makeNetworkCall(
        tag: String = "",
        callBlock: suspend () -> Response<T>
    ): Resource<T>

    suspend fun fetchMultipleChartsData(symbols: String): Resource<LinkedTreeMap<String, ChartsDataResponse>>

    suspend fun getStockSummary(symbol: String): Resource<StockSummaryResponse>

    suspend fun fetchTrendingStocks(): Resource<TrendingStocksResponse>

    suspend fun fetchUpdatedQuotes(symbols: String): Resource<MultipleQuotesResponse>

    fun getProfileFlow(): Flow<Profile?>

    suspend fun insertProfile(profile: Profile)

    suspend fun updateProfile(profile: Profile)

    suspend fun getProfileById(id: Int = 1): Profile?
}