package com.konovus.apitesting.ui.mainScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.data.local.entities.*
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.data.repository.IMainRepository
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Constants.TEN_MINUTES
import com.konovus.apitesting.util.Constants.TIME_SPANS
import com.konovus.apitesting.util.Resource
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: IMainRepository,
) : ViewModel() {

    val profile: LiveData<Profile?> = repository.getProfileFlow().mapLatest { profile ->
        Log.i(TAG, "MVM profile: $profile")
        if (profile != null)
            updateFavoritesUiState(profile.favorites)
        else if (repository.getProfileById(1) == null)
            repository.insertProfile(Profile())
        profile
    }.asLiveData()

    private val favoritesStateFlow = MutableStateFlow(FavoritesUiState())
    val favoritesState: LiveData<FavoritesUiState> = favoritesStateFlow.asLiveData()

    private val _trendingStocks = MutableStateFlow(emptyList<Stock>())
    val trendingStocks: LiveData<List<Stock>> = _trendingStocks.asLiveData()

    private val eventChannel = Channel<String>()
    val event = eventChannel.receiveAsFlow()

    private fun sendEvent(message: String) = viewModelScope.launch {
        eventChannel.send(message)
    }

    private fun updatePortfolioStocksPrices(symbols: String) = viewModelScope.launch {
        val result = repository.fetchUpdatedQuotes(symbols)
        processNetworkResult(result) { list ->
            val responseList = list.quoteResponse.result.filterNot {
                it.toString().contains("null")
            }.map { it.toQuote() }
            responseList.forEach { repository.updatePortfolioStocksCache(it) }
        }
    }

    fun updatePortfolio(profile: Profile) = viewModelScope.launch {
        val portfolio = profile.portfolio
        if (portfolio.stocksToShareAmount.isEmpty() ||
            (portfolio.lastUpdatedTime + TEN_MINUTES > System.currentTimeMillis() && repository.portfolioQuotesCache.isNotEmpty()))
            return@launch

        updatePortfolioStocksPrices(portfolio.stocksToShareAmount.keys.joinToString(","))
        val updatedBalance = repository.portfolioQuotesCache.sumOf {
            it.price.toDouble() * portfolio.stocksToShareAmount[it.symbol]!!
        }.toNDecimals(2)
        if (updatedBalance == 0.0) return@launch
        val initialBalance = portfolio.transactions.filter { it.orderType == OrderType.Buy }.sumOf { it.amount }
            .minus(portfolio.transactions.filter { it.orderType == OrderType.Sell }.sumOf { it.amount }).toNDecimals(2)
        val change = updatedBalance - initialBalance
        val updatedPortfolio = portfolio.copy(
            totalBalance = updatedBalance,
            change = change.toNDecimals(2),
            changePercent = (change / initialBalance * 100).toNDecimals(2),
            lastUpdatedTime = System.currentTimeMillis()
        )
        repository.updateProfile(profile.copy(portfolio = updatedPortfolio))
    }

    private fun updateFavoritesUiState(symbols: List<String>) {
        favoritesStateFlow.update { it.copy(
            symbols = symbols,
            quotes = repository.favoritesCache.filter { symbols.contains(it.symbol) },
            chartData = repository.chartDataCache,
            hasFavorites = symbols.isNotEmpty()
        ) }
    }

    fun updateFavoritesQuotes() = viewModelScope.launch {
        val quotesAreUpdated = repository.favoritesCache.isNotEmpty() &&
                repository.favoritesCache.minOf { it.lastTimeUpdated } + TEN_MINUTES > System.currentTimeMillis()
        if (!favoritesStateFlow.value.hasFavorites || favoritesStateFlow.value.isFetchingQuotes || quotesAreUpdated) return@launch

        favoritesStateFlow.update { it.copy(isFetchingQuotes = true) }
        val responseMultipleQuotes = repository
            .fetchUpdatedQuotes(favoritesStateFlow.value.symbols.joinToString(","))
        processNetworkResult(responseMultipleQuotes) { result ->
            val quotes = result.quoteResponse.result.map { data -> data.toQuote() }
            favoritesStateFlow.update { it.copy(quotes = quotes, isFetchingQuotes = false) }
            repository.updateFavoritesCache(quotes)
        }
    }

    fun updateFavoritesChartData() = viewModelScope.launch {
        if (!favoritesStateFlow.value.hasFavorites || repository.chartDataCache.isNotEmpty()
            || favoritesStateFlow.value.isFetchingChartData) return@launch

        favoritesStateFlow.update { it.copy(isFetchingChartData = true) }
        val responseChartData = repository
            .fetchMultipleChartsData(favoritesStateFlow.value.symbols.joinToString(","))
        processNetworkResult(responseChartData) { result ->
            val updatedChartData: MutableMap<String, List<ChartData>> = mutableMapOf()
            result.values.forEach {
                val key = it.symbol + TIME_SPANS[0].first + TIME_SPANS[0].second
                val value = it.close.mapIndexed { index, close -> ChartData(
                    close = close,
                    timestamp = it.timestamp[index].toString()
                ) }
                updatedChartData[key] = value
                repository.updateChartDataCache(key = key, value = value)
            }
            favoritesStateFlow.update { it.copy(chartData = repository.chartDataCache, isFetchingChartData = false) }
        }
    }

    fun getTrendingStocks() = viewModelScope.launch {
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
                is Resource.Success -> processBlock(result.data!!)
                is Resource.Loading -> {}
                is Resource.Error -> {
                    sendEvent(message = result.message.orEmpty())
                }
            }
        }
    }

    data class FavoritesUiState(
        val symbols: List<String> = emptyList(),
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


