package com.konovus.apitesting.ui.infoScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.data.local.entities.*
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.repository.AlphaVantageRepository
import com.konovus.apitesting.data.repository.MainRepository
import com.konovus.apitesting.util.Constants.TEN_MINUTES
import com.konovus.apitesting.util.Constants.TIME_SPANS
import com.konovus.apitesting.util.NetworkStatus
import com.konovus.apitesting.util.Resource
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alphaVantageRepository: AlphaVantageRepository,
    private val repository: MainRepository,
    val store: Store<AppState>
) : ViewModel() {

    private var stateFlow = MutableStateFlow(InfoScreenStates())
    val state = stateFlow.asLiveData()

    private val eventChannel = Channel<String>()
    val event = eventChannel.receiveAsFlow()

    private val symbol = savedStateHandle.get<String>("symbol")!!

    init {
        observeConnectivity()
        initSetup()
    }

    private fun sendEvent(message: String) = viewModelScope.launch {
        eventChannel.send(message)
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            store.stateFlow.map { it.networkStatus }.distinctUntilChanged().collectLatest {
                if (it == NetworkStatus.BackOnline)
                    initSetup()
            }
        }
    }

    private fun initSetup() {
        collectPortfolio()
        val cacheHasStock = repository.stocksCache.map { it.symbol }.contains(symbol)
        val stockIsUpdated = (repository.stocksCache.find { it.symbol == symbol }?.lastUpdatedTime ?: 0) + TEN_MINUTES > System.currentTimeMillis()
        if (cacheHasStock && stockIsUpdated)
            stateFlow.update { it.copy(
                stock = repository.stocksCache.find { it.symbol == symbol }!!.copy(
                isFavorite = repository.favoritesCache.map { it.symbol }.contains(symbol))) }
        else getStockSummary(symbol)

        val key = symbol + TIME_SPANS[0].first + TIME_SPANS[0].second
        if (repository.chartDataCache.containsKey(key))
            stateFlow.update { it.copy(chartData = repository.chartDataCache[key]) }
        else getCurrentChartData(symbol)
    }

    private fun collectPortfolio() = viewModelScope.launch {
        repository.getPortfoliosFlow().collectLatest { portfolios ->
            stateFlow.update { it.copy(portfolio = portfolios.first()) }
        }
    }

    private fun getCurrentChartData(symbol: String, pos: Int = 0) = viewModelScope.launch {
        stateFlow.update { it.copy(chartLoading = true) }
        val chartDataResult = alphaVantageRepository.getChartData(symbol, TIME_SPANS[pos])
        processNetworkResult(chartDataResult) { chartData ->
            repository.updateChartDataCache(symbol + TIME_SPANS[pos].first + TIME_SPANS[pos].second, chartData)
            stateFlow.update { it.copy(
                chartData = chartData.ifEmpty { null },
                chartLoading = false,
                stock = updateStockChartChange(chartData)
            ) }
        }
    }

    private fun getStockSummary(symbol: String) = viewModelScope.launch {
        stateFlow.update { it.copy(isLoading = true) }
        val stockSummaryResult = repository.getStockSummary(symbol)
        processNetworkResult(stockSummaryResult) { stockResponse ->
            val updatedStock = stockResponse.toStock().copy(
                isFavorite = repository.favoritesCache.map { it.symbol }.contains(symbol)
            )
            repository.updateStocksCache(updatedStock)
            stateFlow.update { it.copy(
                stock = updatedStock,
                isLoading = false
            ) }
        }
    }

    fun onEvent(event: InfoScreenEvent) {
        when (event) {
            is InfoScreenEvent.OnMarketOrderEvent -> {
                viewModelScope.launch {
                    stateFlow.value.portfolio?.let { portfolio ->
                        val transactions = portfolio.transactions.toMutableList()
                        transactions.add(event.transaction)

                        val updatedPortfolio = portfolio.copy(
                            transactions = transactions,
                            totalBalance = (portfolio.totalBalance + if(event.transaction.orderType == OrderType.Buy)
                                event.transaction.amount else - event.transaction.amount).toNDecimals(2)
                        )
                        stateFlow.value.stock?.let { repository.updatePortfolioStocksCache(it.toQuote()) }
                        repository.insertPortfolio(portfolio = updatedPortfolio)
                    }
                }
            }
            is InfoScreenEvent.OnRetry -> initSetup()
            is InfoScreenEvent.OnFavorite -> {
                viewModelScope.launch {
                    event.stock?.let {
                        val updatedStock = it.copy(isFavorite = !it.isFavorite)
                        stateFlow.update { it.copy(stock = updatedStock) }
                        repository.insertStock(updatedStock)
                    }
                }
            }
            is InfoScreenEvent.OnChangeChartTimeSpan -> {
                viewModelScope.launch {
                    stateFlow.update { it.copy(chartLoading = true) }
                    val key = symbol + TIME_SPANS[event.pos].first + TIME_SPANS[event.pos].second
                    if (repository.chartDataCache.containsKey(key)) {
                        stateFlow.update { it.copy(
                            chartData = repository.chartDataCache[key],
                            chartLoading = false,
                            stock = if (event.pos == 0)
                                repository.stocksCache.find { it.symbol == symbol }
                            else updateStockChartChange(repository.chartDataCache[key]!!)
                        ) }
                    } else getCurrentChartData(symbol, event.pos)
                }
            }
            is InfoScreenEvent.OnChangeTab -> stateFlow.update { it.copy(tabNr = event.nr) }
        }
    }

    private fun updateStockChartChange(list: List<ChartData>): Stock? {
        val change = (list.last().close - list.first().close).toNDecimals(2)
        val changeInPercent = (change / list.first().close * 100).toNDecimals(2)
        return stateFlow.value.stock?.copy(
            chartChange = Stock.ChartChange(change,changeInPercent)
        )
    }

    private fun <T> processNetworkResult(
        result: Resource<T>,
        processBlock: suspend (T) -> Unit
    ) {
        viewModelScope.launch {
            when (result) {
                is Resource.Success -> {
                    result.data?.let { processBlock(it) }
                }
                is Resource.Loading -> stateFlow.value = stateFlow.value.copy(isLoading = true)
                is Resource.Error -> {
                    sendEvent(message = result.message.orEmpty())
                    stateFlow.update { it.copy(isLoading = false, chartLoading = false) }
                }
            }
        }
    }

    data class InfoScreenStates(
        val stock: Stock? = null,
        val chartData: List<ChartData>? = null,
        val portfolio: Portfolio? = null,
        val isLoading: Boolean = false,
        val chartLoading: Boolean = false,
        val error: String? = null,
        val tabNr: Int = 0
    )
}

sealed class InfoScreenEvent {
    data class OnMarketOrderEvent(val transaction: Transaction) : InfoScreenEvent()
    object OnRetry : InfoScreenEvent()
    data class OnFavorite(val stock: Stock?) : InfoScreenEvent()
    data class OnChangeChartTimeSpan(val pos: Int): InfoScreenEvent()
    data class OnChangeTab(val nr: Int): InfoScreenEvent()
}
