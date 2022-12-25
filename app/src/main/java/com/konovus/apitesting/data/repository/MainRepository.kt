package com.konovus.apitesting.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.konovus.apitesting.data.api.YhFinanceApi
import com.konovus.apitesting.data.local.dao.PortfolioDao
import com.konovus.apitesting.data.local.dao.ProfileDao
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Profile
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.data.remote.responses.ChartsDataResponse
import com.konovus.apitesting.data.remote.responses.MultipleQuotesResponse
import com.konovus.apitesting.data.remote.responses.StockSummaryResponse
import com.konovus.apitesting.data.remote.responses.TrendingStocksResponse
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Resource
import kotlinx.coroutines.flow.Flow
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
    private val yhFinanceApi: YhFinanceApi,
    private val profileDao: ProfileDao,
) : IMainRepository {

    // Mutex to make writes to cached values thread-safe.
    private val mutex = Mutex()

    private val _favoritesCache = mutableListOf<Quote>()
    override val favoritesCache: List<Quote>
        get() = _favoritesCache.toList()

    private val _stocksCache = mutableListOf<Stock>()
    override val stocksCache: List<Stock>
        get() = _stocksCache.toList()

    private val _chartDataCache = mutableMapOf<String, List<ChartData>>()
    override val chartDataCache: Map<String, List<ChartData>>
        get() = _chartDataCache.toMap()

     private val _portfolioQuotesCache = mutableListOf<Quote>()
     override val portfolioQuotesCache: List<Quote>
        get() = _portfolioQuotesCache.toList()

    override suspend fun updateFavoritesCache(quotes: List<Quote>) = mutex.withLock {
        _favoritesCache.clear()
        _favoritesCache.addAll(quotes)
    }

    override suspend fun updateFavoritesCacheQuote(quote: Quote): Unit = mutex.withLock {
        _favoritesCache.removeIf { quote.symbol == it.symbol }
        _favoritesCache.add(quote)
    }

    override suspend fun updatePortfolioStocksCache(quote: Quote) = mutex.withLock {
        _portfolioQuotesCache.removeIf { it.symbol == quote.symbol }
        _portfolioQuotesCache.add(quote)
    }

    override suspend fun removeFromPortfolioStocksCache(symbol: String) {
        _portfolioQuotesCache.removeIf { it.symbol == symbol }
    }

    override suspend fun updateChartDataCache(key: String, value: List<ChartData>) = mutex.withLock {
        _chartDataCache[key] = value
    }

    override suspend fun updateStocksCache(stock: Stock) = mutex.withLock {
        _stocksCache.removeIf { it.symbol == stock.symbol }
        _stocksCache.add(stock)
    }

    override suspend fun <T> makeNetworkCall(
        tag: String,
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
            Resource.Error("JsonSyntax Error. ${e.message}", null)
        } catch (e: HttpException) {
            Resource.Error("Couldn't reach the server. Check your internet connection.", null)
        } catch (e: Exception) {
            Resource.Error("Couldn't reach the server. Check your internet connection.", null)
        }
    }

    override suspend fun fetchMultipleChartsData(symbols: String): Resource<LinkedTreeMap<String, ChartsDataResponse>> {
        return makeNetworkCall("charts $symbols") {
            yhFinanceApi.fetchMultipleChartsData(symbols)
        }
    }

    override suspend fun getStockSummary(symbol: String): Resource<StockSummaryResponse> = makeNetworkCall(symbol) {
        yhFinanceApi.getStockSummary(symbol)
    }

    override suspend fun fetchTrendingStocks(): Resource<TrendingStocksResponse> = makeNetworkCall("trending") {
        yhFinanceApi.fetchTrendingStocks()
    }

    override suspend fun fetchUpdatedQuotes(symbols: String): Resource<MultipleQuotesResponse> {
        return makeNetworkCall(symbols) {
            yhFinanceApi.fetchMultipleQuotes(symbols)
        }
    }

    override fun getFavoritesFlow(): Flow<List<Stock>> = stockDao.getAllFavoriteStocksFlow()

    override fun getFavoritesNr(): Flow<Int> = stockDao.getTotalFavStocks()

    override suspend fun insertStock(stock: Stock) = stockDao.insertStock(stock)

    override suspend fun getStock(symbol: String): Stock? = stockDao.getStockBySymbol(symbol)

    override suspend fun insertPortfolio(portfolio: Portfolio) = portfolioDao.insertPortfolio(portfolio)

    override suspend fun updatePortfolio(portfolio: Portfolio) = portfolioDao.updatePortfolio(portfolio)

    override fun getPortfolioFlow() = portfolioDao.getPortfolioFlow()

    override suspend fun getPortfolio() = portfolioDao.getPortfolioById()

    override fun getProfileFlow(): Flow<Profile> = profileDao.getProfileFlow()

    override suspend fun insertProfile(profile: Profile) = profileDao.insert(profile)

    override suspend fun updateProfile(profile: Profile) = profileDao.update(profile)

    override suspend fun getProfile(): Profile? = profileDao.getProfile()

}