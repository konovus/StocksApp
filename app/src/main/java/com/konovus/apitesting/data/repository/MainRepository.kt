package com.konovus.apitesting.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.konovus.apitesting.data.api.YhFinanceApi
import com.konovus.apitesting.data.local.dao.PortfolioDao
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Resource
import com.konovus.apitesting.util.toNDecimals
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val stockDao: StockDao,
    private val portfolioDao: PortfolioDao,
    private val yhFinanceApi: YhFinanceApi,
    private val store: Store<AppState>
) {

    suspend fun <T> makeNetworkCall(
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
            Log.i(TAG, "makeNetworkCall: Unknown Error: ${e.message} , ${e.cause}.")
            Resource.Error("Couldn't reach the server. Check your internet connection.", null)
        }
    }



    private suspend fun getUpdatedStockList(portfolio: Portfolio): List<Stock> {
        if (portfolio.stocksToShareAmount.isEmpty()) return emptyList()
        val result = makeNetworkCall("updatePortfolioStockPrices") {
            yhFinanceApi.getMultipleQuotes(portfolio.stocksToShareAmount.keys.joinToString(","))
        }
        if (result.data == null) return emptyList()
        val responseList = result.data.quoteResponse.result.map { Pair(it.symbol, it.regularMarketPrice) }
        val localList = result.data.quoteResponse.result.filterNot {
            it.toString().contains("null")
        }.mapNotNull { getLocalStockBySymbol(it.symbol) }

        val updatedList = localList.map { stock ->
            stock.copy(price = responseList.find { it.first == stock.symbol }?.second ?: stock.price,
                lastUpdatedTime = System.currentTimeMillis())
        }
        insertStocks(updatedList)
        return updatedList
    }

    suspend fun updatePortfolioStocksPrices(portfolio: Portfolio) {
        Log.i(TAG, "updatePortfolioStocksPrices...")
        val updatedList = getUpdatedStockList(portfolio)
        var updatedBalance = 0.0
        updatedList.forEach {
            updatedBalance += it.price * portfolio.stocksToShareAmount[it.symbol]!!
        }
        if (updatedBalance == 0.0) return
        val initialBalance = portfolio.transactions.filter { it.orderType == OrderType.Buy }.sumOf { it.amount }
            .minus(portfolio.transactions.filter { it.orderType == OrderType.Sell }.sumOf { it.amount })
        val change = updatedBalance - initialBalance
        val updatedPortfolio = portfolio.copy(
            totalBalance = updatedBalance.toNDecimals(2),
            change = change.toNDecimals(2),
            changePercent = (change / initialBalance * 100).toNDecimals(2),
            lastUpdatedTime = System.currentTimeMillis()
        )
        store.update { it.copy(portfolio = updatedPortfolio) }
        updatePortfolio(portfolio = updatedPortfolio)
    }

    suspend fun getPortfolioById(id: Int): Portfolio? = portfolioDao.getPortfolioById(id)

    private suspend fun updatePortfolio(portfolio: Portfolio) = portfolioDao.updatePortfolio(portfolio)

    fun getFavoritesFlow() = stockDao.getAllFavoriteStocksFlow()

    suspend fun getFavoritesNr() = stockDao.getTotalFavStocks()

    suspend fun getLocalStockBySymbol(symbol: String) = stockDao.getStockBySymbol(symbol)

    suspend fun insertStock(stock: Stock) = stockDao.insertStock(stock)

    suspend fun insertStocks(stocks: List<Stock>) = stockDao.insertStocks(stocks)

    suspend fun updateStock(stock: Stock) = stockDao.updateStock(stock)

    suspend fun updateStocks(stocks: List<Stock>) = stockDao.updateStocks(stocks)

    suspend fun insertPortfolio(portfolio: Portfolio) = portfolioDao.insertPortfolio(portfolio)

    fun portfolioCount() = portfolioDao.portfolioCount()

    fun getPortfoliosFlow() = portfolioDao.getAllPortfolios()

}