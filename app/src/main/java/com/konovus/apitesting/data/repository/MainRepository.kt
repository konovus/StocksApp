package com.konovus.apitesting.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.konovus.apitesting.data.api.TwelveApi
import com.konovus.apitesting.data.local.dao.PortfolioDao
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.remote.responses.PriceResponse
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Resource
import com.konovus.apitesting.util.toNDecimals
import kotlinx.coroutines.delay
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val stockDao: StockDao,
    private val portfolioDao: PortfolioDao,
    private val twelveApi: TwelveApi,
    private val store: Store<AppState>
) {

    suspend fun <T> makeNetworkCall(
        tag: String = "",
        callBlock: suspend () -> Response<T>
    ): Resource<T> {
        var response: Response<T>? = null
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
            Resource.Error("Api limit reached, wait 1 minute.", null)
        } catch (e: HttpException) {
            Log.i(TAG, "makeNetworkCall Response: ${response?.code()} , ${response?.errorBody()} , ${response?.body()}")
            Log.i(TAG, "makeNetworkCall Error: $tag , ${e.message} , ${e.localizedMessage}")
            Resource.Error("Couldn't reach the server. Check your internet connection.", null)
        } catch (e: Exception) {
            Log.i(TAG, "makeNetworkCall: Unknown Error: ${e.cause}.")
            Resource.Error("Couldn't reach the server. Check your internet connection.", null)
        }
    }

    suspend fun updatePortfolioStocksPrices(portfolio: Portfolio) {
        val updatedBalance = if (portfolio.stocksToShareAmount.keys.size == 1)
            getUpdatedBalanceForSingleStock(portfolio) else getUpdatedBalanceForMultipleStocks(portfolio)
        if (updatedBalance == 0.0) return
        val initialBalance = portfolio.transactions.filter { it.orderType == OrderType.Buy }.sumOf { it.amount }
            .minus(portfolio.transactions.filter { it.orderType == OrderType.Sell }.sumOf { it.amount })
        val change = updatedBalance - initialBalance
        val updatedPortfolio = portfolio.copy(
            totalBalance = updatedBalance.toNDecimals(2),
            change = change.toNDecimals(2),
            changeInPercentage = (change / initialBalance * 100).toNDecimals(2),
            lastUpdatedTime = System.currentTimeMillis()
        )
        store.update { it.copy(portfolio = updatedPortfolio) }
        updatePortfolio(portfolio = updatedPortfolio)
    }

    private suspend fun getUpdatedBalanceForMultipleStocks(portfolio: Portfolio): Double {
        var updatedBalance = 0.0
        val nr = portfolio.stocksToShareAmount.keys.size
        val localList = mutableListOf<Stock>()
        val updatedList = mutableListOf<Stock>()
        var result: Resource<LinkedTreeMap<String, PriceResponse>>?
        for (i in 0..nr step 8) {
            val symbols = portfolio.stocksToShareAmount.keys.filterIndexed { index, _ -> index in i..i+7}
                .joinToString(",")
            result = makeNetworkCall(symbols) {
                twelveApi.getPricesForStocks(symbols)
            }
            if (result.data == null) return updatedBalance
            localList.addAll(
                result.data!!.filterNot {
                it.value.toString().contains("null")
                }.mapNotNull { getLocalStockBySymbol(it.key) }
            )

            localList.map { localStock ->
                updatedList.add( localStock.copy(
                    price = result.data!![localStock.symbol]!!.price.toDouble().toNDecimals(2)
                    )
                )
            }

            localList.clear()
            if (nr > 8 && i <= nr - 8)
                delay(60 * 1000)
        }

        insertStocks(updatedList)
        updatedList.forEach {
            updatedBalance += it.price * portfolio.stocksToShareAmount[it.symbol]!!
        }
        return updatedBalance
    }

    private suspend fun getUpdatedBalanceForSingleStock(portfolio: Portfolio): Double {
        val localStock = getLocalStockBySymbol(portfolio.stocksToShareAmount.keys.first())!!
        val result = makeNetworkCall("portfolio stock price update") {
            twelveApi.getPriceForStock(portfolio.stocksToShareAmount.keys.first())
        }
        val price = if (result.data == null || result.data.values.first().contains("null"))
            localStock.price
        else result.data.values.first().toDouble().toNDecimals(2)
        insertStock(localStock.copy(price = price))
        return price * portfolio.stocksToShareAmount.values.first()
    }

    suspend fun getPortfolioById(id: Int): Portfolio? = portfolioDao.getPortfolioById(id)

    private suspend fun updatePortfolio(portfolio: Portfolio) = portfolioDao.updatePortfolio(portfolio)

    fun getFavoritesFlow() = stockDao.getAllFavoriteStocksFlow()

    suspend fun getLocalStockBySymbol(symbol: String) = stockDao.getStockBySymbol(symbol)

    suspend fun insertStock(stock: Stock) = stockDao.insertStock(stock)

    suspend fun insertStocks(stocks: List<Stock>) = stockDao.insertStocks(stocks)

    suspend fun updateStock(stock: Stock) = stockDao.updateStock(stock)

    suspend fun insertPortfolio(portfolio: Portfolio) = portfolioDao.insertPortfolio(portfolio)


}