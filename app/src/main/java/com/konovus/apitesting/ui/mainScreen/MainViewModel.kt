package com.konovus.apitesting.ui.mainScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.repository.MainRepository
import com.konovus.apitesting.util.Constants.TEN_MINUTES
import com.konovus.apitesting.util.Constants.TIME_SPANS
import com.konovus.apitesting.util.Resource
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    val store: Store<AppState>,
    app: Application
) : AndroidViewModel(app) {

    private val _portfolio = MutableStateFlow(Portfolio())
    val portfolio: LiveData<Portfolio> = _portfolio.asLiveData()

    private val favoritesStateFlow = MutableStateFlow(FavoritesUiState())
    val favoritesState: LiveData<FavoritesUiState> = favoritesStateFlow.asLiveData()

    private val _trendingStocks = MutableStateFlow(emptyList<Stock>())
    val trendingStocks: LiveData<List<Stock>> = _trendingStocks.asLiveData()

    private val eventChannel = Channel<String>()
    val event = eventChannel.receiveAsFlow()


    fun initSetup() {
        createDefaultPortfolio()
        collectPortfolio()
        collectFavorites()
        getTrendingStocks()
    }

    private fun sendEvent(message: String) = viewModelScope.launch {
        eventChannel.send(message)
    }

    private fun updatePortfolioStocksPrices() = viewModelScope.launch {
        val symbols = _portfolio.value.stocksToShareAmount.keys.joinToString(",")
        val result = repository.fetchUpdatedQuotes(symbols)
        processNetworkResult(result) { list ->
            val responseList = list.quoteResponse.result.filterNot {
                it.toString().contains("null")
            }.map { it.toQuote() }
            responseList.forEach { repository.updatePortfolioStocksCache(it) }
        }
    }

    fun updatePortfolio() = viewModelScope.launch {
        val portfolio = _portfolio.value
        if (portfolio.stocksToShareAmount.isEmpty() ||
            (portfolio.lastUpdatedTime + TEN_MINUTES > System.currentTimeMillis() && repository.portfolioQuotesCache.isNotEmpty()))
            return@launch
        updatePortfolioStocksPrices()
        val updatedBalance = repository.portfolioQuotesCache.sumOf {
            it.price.toDouble() * portfolio.stocksToShareAmount[it.symbol]!!
        }
        if (updatedBalance == 0.0) return@launch
        val initialBalance = portfolio.transactions.filter { it.orderType == OrderType.Buy }.sumOf { it.amount }
            .minus(portfolio.transactions.filter { it.orderType == OrderType.Sell }.sumOf { it.amount })
        val change = updatedBalance - initialBalance
        val updatedPortfolio = portfolio.copy(
            totalBalance = updatedBalance.toNDecimals(2),
            change = change.toNDecimals(2),
            changePercent = (change / initialBalance * 100).toNDecimals(2),
            lastUpdatedTime = System.currentTimeMillis()
        )
        repository.updatePortfolio(portfolio = updatedPortfolio)
    }

    private fun collectFavorites() = viewModelScope.launch {
        val favoritesNr = repository.getFavoritesNr()
        favoritesStateFlow.update { it.copy(hasFavorites = favoritesNr > 0) }
        repository.getFavoritesFlow().filterNotNull().collectLatest { list ->
            favoritesStateFlow.update { it.copy(
                quotes = list.sortedByDescending { it.id }.map { stock -> stock.toQuote()},
                chartData = repository.chartDataCache,
                hasFavorites = list.isNotEmpty()) }
        }
    }

    fun updateFavoritesQuotes() = viewModelScope.launch {
        if (favoritesStateFlow.value.isFetchingQuotes || favoritesStateFlow.value.quotes.isNullOrEmpty()
            || favoritesStateFlow.value.quotes!!.minOf { it.lastTimeUpdated } + TEN_MINUTES > System.currentTimeMillis()) return@launch
        favoritesStateFlow.update { it.copy(isFetchingQuotes = true) }
        val responseMultipleQuotes = repository
            .fetchUpdatedQuotes(repository.favoritesCache.joinToString(",") { it.symbol })
        processNetworkResult(responseMultipleQuotes) { result ->
            val quotes = result.quoteResponse.result.map { data -> data.toQuote() }
            favoritesStateFlow.update { it.copy(quotes = quotes, isFetchingQuotes = false) }
        }
    }

    fun updateFavoritesChartData() = viewModelScope.launch {
        if (repository.favoritesCache.isEmpty() || repository.chartDataCache.isNotEmpty()
            || favoritesStateFlow.value.isFetchingChartData) return@launch
        favoritesStateFlow.update { it.copy(isFetchingChartData = true) }
        val responseChartData = repository
            .fetchMultipleChartsData(repository.favoritesCache.joinToString(",") { it.symbol })
        processNetworkResult(responseChartData) { result ->
            result.values.forEach {
                val key = it.symbol + TIME_SPANS[0].first + TIME_SPANS[0].second
                val value = it.close.mapIndexed { index, close -> ChartData(
                    close = close,
                    timestamp = it.timestamp[index].toString()
                ) }
                repository.updateChartDataCache(key = key, value = value)
            }
        }
        favoritesStateFlow.update { it.copy(chartData = repository.chartDataCache, isFetchingChartData = false) }
    }

    private fun getTrendingStocks() = viewModelScope.launch {
        if (_trendingStocks.value.isNotEmpty()) return@launch
        val response = repository.fetchTrendingStocks()
        processNetworkResult(response) { result ->
            val stocks = result.finance.result.first().quotes.map { it.toStock() }
                .filter { it.quoteType == "EQUITY" }
            if (stocks.isEmpty()) sendEvent("No trending stocks found.")
            _trendingStocks.value = stocks
        }
    }

    private fun <T> processNetworkResult(
        result: Resource<T>,
        processBlock: suspend (T) -> Unit
    ) {
        viewModelScope.launch {
            when (result) {
                is Resource.Success -> {
                    processBlock(result.data!!)
                }
                is Resource.Loading -> {}
                is Resource.Error -> {
                    sendEvent(message = result.message.orEmpty())
                }
            }
        }
    }

    private fun createDefaultPortfolio() = viewModelScope.launch {
        if (repository.getPortfolio() == null)
            repository.insertPortfolio(Portfolio(name = "Default", id = 1))
    }

    private fun collectPortfolio() = viewModelScope.launch {
        repository.getPortfoliosFlow().filterNotNull().collectLatest { list ->
            if (list.isEmpty()) return@collectLatest
            _portfolio.update { list.first() }
        }
    }

    data class FavoritesUiState(
        val quotes: List<Quote>? = null,
        val chartData: Map<String, List<ChartData>>? = null,
        val isFetchingQuotes: Boolean = false,
        val isFetchingChartData: Boolean = false,
        val hasFavorites: Boolean = false
    )

    sealed class Event{
        data class ErrorEvent(val message: String): Event()
    }
}


