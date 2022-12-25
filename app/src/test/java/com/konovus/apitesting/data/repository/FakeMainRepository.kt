package com.konovus.apitesting.data.repository

import com.google.gson.internal.LinkedTreeMap
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.data.remote.responses.ChartsDataResponse
import com.konovus.apitesting.data.remote.responses.MultipleQuotesResponse
import com.konovus.apitesting.data.remote.responses.StockSummaryResponse
import com.konovus.apitesting.data.remote.responses.TrendingStocksResponse
import com.konovus.apitesting.util.Resource
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class FakeMainRepository(
    val stockDao: StockDao
): IMainRepository {

    override val favoritesCache: List<Stock>
        get() = TODO("Not yet implemented")
    override val stocksCache: List<Stock>
        get() = TODO("Not yet implemented")
    override val chartDataCache: Map<String, List<ChartData>>
        get() = TODO("Not yet implemented")
    override val portfolioQuotesCache: List<Quote>
        get() = TODO("Not yet implemented")

    override suspend fun updateFavoritesCache(stocks: List<Stock>): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updatePortfolioStocksCache(quote: Quote): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updateChartDataCache(key: String, value: List<ChartData>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateStocksCache(stock: Stock): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun <T> makeNetworkCall(
        tag: String,
        callBlock: suspend () -> Response<T>
    ): Resource<T> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchMultipleChartsData(symbols: String): Resource<LinkedTreeMap<String, ChartsDataResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun getStockSummary(symbol: String): Resource<StockSummaryResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchTrendingStocks(): Resource<TrendingStocksResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchUpdatedQuotes(symbols: String): Resource<MultipleQuotesResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePortfolio(portfolio: Portfolio) {
        TODO("Not yet implemented")
    }

    override fun getFavoritesFlow(): Flow<List<Stock>> {
        TODO("Not yet implemented")
    }

    override fun getFavoritesNr(): Flow<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun insertStock(stock: Stock) {
        TODO("Not yet implemented")
    }

    override suspend fun insertPortfolio(portfolio: Portfolio) {
        TODO("Not yet implemented")
    }

    override fun getPortfolioFlow(): Flow<List<Portfolio>> {
        TODO("Not yet implemented")
    }

    override suspend fun getPortfolio(): Portfolio? {
        TODO("Not yet implemented")
    }
}