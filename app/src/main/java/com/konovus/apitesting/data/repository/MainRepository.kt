package com.konovus.apitesting.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.konovus.apitesting.data.api.YhFinanceApi
import com.konovus.apitesting.data.local.dao.PortfolioDao
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.data.remote.responses.ChartsData
import com.konovus.apitesting.data.remote.responses.MultipleQuotesResponse
import com.konovus.apitesting.data.remote.responses.StockSummaryResponse
import com.konovus.apitesting.data.remote.responses.TrendingStocksResponse
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val stockDao: StockDao,
    private val portfolioDao: PortfolioDao,
    private val yhFinanceApi: YhFinanceApi
) {

    // Mutex to make writes to cached values thread-safe.
    private val mutex = Mutex()

    private val _favoritesCache = mutableListOf<Stock>()
    val favoritesCache: List<Stock>
        get() = _favoritesCache.toList()

    private val _stocksCache = mutableListOf<Stock>()
    val stocksCache: List<Stock>
        get() = _stocksCache.toList()

    private val _chartDataCache = mutableMapOf<String, List<ChartData>>()
    val chartDataCache: Map<String, List<ChartData>>
        get() = _chartDataCache.toMap()

    private val _portfolioQuotesCache = mutableListOf<Quote>()
    val portfolioQuotesCache: List<Quote>
        get() = _portfolioQuotesCache.toList()

    suspend fun updatePortfolioStocksCache(quote: Quote) = mutex.withLock {
        _portfolioQuotesCache.removeIf { it.symbol == quote.symbol }
        _portfolioQuotesCache.add(quote)
    }

    suspend fun updateChartDataCache(key: String, value: List<ChartData>) = mutex.withLock {
        _chartDataCache[key] = value
    }

    suspend fun updateStocksCache(stock: Stock) = mutex.withLock {
        _stocksCache.removeIf { it.symbol == stock.symbol }
        _stocksCache.add(stock)
    }


    private suspend fun <T> makeNetworkCall(
        tag: String = "",
        callBlock: suspend () -> Response<T>
    ): Resource<T> {
        val response: Response<T>?
        return try {
            Log.i(TAG, "making a network call for $tag...")
            response = callBlock()
            if (!response.isSuccessful){
                val gson = Gson()
                val type = object : TypeToken<T>() {}.type
                val errorResponse: T? = gson.fromJson(response.errorBody()!!.charStream(), type)
                return Resource.Error("Error ${response.code()}: $errorResponse")
            }
            response.body()?.let {
                Resource.Success(data = it)
            } ?: Resource.Error("Null response body, try again.")
        } catch (e: JsonSyntaxException) {
            Log.i(TAG, "JsonSyntax Error. ${e.message}")
            Resource.Error("JsonSyntax Error. ${e.message}", null)
        } catch (e: HttpException) {
            Log.i(TAG, "makeNetworkCall Error: $tag , ${e.message} , ${e.localizedMessage}")
            Resource.Error("Couldn't reach the server. Check your internet connection.", null)
        } catch (e: Exception) {
            Log.i(TAG, "makeNetworkCall: Unknown Error: ${e.message} , ${e.localizedMessage}.")
            Resource.Error("Couldn't reach the server. Check your internet connection.", null)
        }
    }

    suspend fun fetchMultipleChartsData(symbols: String): Resource<LinkedTreeMap<String, ChartsData>> {
        return makeNetworkCall("charts $symbols") {
            yhFinanceApi.fetchMultipleChartsData(symbols)
        }
    }

    suspend fun getStockSummary(symbol: String): Resource<StockSummaryResponse> = makeNetworkCall(symbol) {
        yhFinanceApi.getStockSummary(symbol)
    }
    suspend fun fetchTrendingStocks(): Resource<TrendingStocksResponse> = makeNetworkCall("trending") {
        yhFinanceApi.fetchTrendingStocks()
    }

    suspend fun fetchUpdatedQuotes(symbols: String): Resource<MultipleQuotesResponse> {
        return makeNetworkCall(symbols) {
            yhFinanceApi.fetchMultipleQuotes(symbols)
        }
    }

    suspend fun updatePortfolio(portfolio: Portfolio) = portfolioDao.updatePortfolio(portfolio)

    fun getFavoritesFlow(): Flow<List<Stock>> {
        return stockDao.getAllFavoriteStocksFlow().map { list ->
            _favoritesCache.clear()
            _favoritesCache.addAll(list)
            list
        }
    }


    suspend fun getFavoritesNr() = stockDao.getTotalFavStocks()

    suspend fun insertStock(stock: Stock) = stockDao.insertStock(stock)

    suspend fun insertStocks(stocks: List<Stock>) = stockDao.insertStocks(stocks)

    suspend fun getStockBySymbol(symbol: String) = stockDao.getStockBySymbol(symbol)

    suspend fun updateStocks(stocks: List<Stock>) = stockDao.updateStocks(stocks)

    suspend fun insertPortfolio(portfolio: Portfolio) = portfolioDao.insertPortfolio(portfolio)

    fun getPortfoliosFlow() = portfolioDao.getAllPortfolios()

    suspend fun getPortfolio() = portfolioDao.getPortfolioById()

}