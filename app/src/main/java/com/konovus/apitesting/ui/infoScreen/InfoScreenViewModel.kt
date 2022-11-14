package com.konovus.apitesting.ui.infoScreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.data.api.YhFinanceApi
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.entities.Transaction
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.repository.AlphaVantageRepository
import com.konovus.apitesting.data.repository.MainRepository
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Constants.TEN_MINUTES
import com.konovus.apitesting.util.Constants.TIME_SPANS
import com.konovus.apitesting.util.NetworkStatus
import com.konovus.apitesting.util.Resource
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alphaVantageRepository: AlphaVantageRepository,
    private val repository: MainRepository,
    private val yhFinanceApi: YhFinanceApi,
    val store: Store<AppState>
) : ViewModel() {

    private var stateFlow = MutableStateFlow(InfoScreenStates())
    val state = stateFlow.asLiveData()

    private val eventChannel = Channel<String>()
    val event = eventChannel.receiveAsFlow()

    val symbol = savedStateHandle.get<String>("symbol")!!

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
        viewModelScope.launch {
            stateFlow.value = stateFlow.value.copy(isLoading = true, chartLoading = true)
            store.read {
                val storeContainsStock = it.stockList.map { it.symbol }.contains(symbol)
                val stockIsUpdated = (it.stockList.find { it.symbol == symbol }?.lastUpdatedTime ?: 0) + TEN_MINUTES > System.currentTimeMillis()
                Log.i(TAG, "initSetup: $storeContainsStock && $stockIsUpdated")
                if (storeContainsStock && stockIsUpdated)
                    stateFlow.value = stateFlow.value.copy(
                        stock = it.stockList.find { it.symbol == symbol }!!.copy(
                            isFavorite = it.favorites.contains(symbol)),
                        isLoading = false)
                else getStockSummary(symbol)

                if (it.chartData.containsKey(symbol + TIME_SPANS[0].first + TIME_SPANS[0].second))
                    stateFlow.value = stateFlow.value.copy(
                        chartData = it.chartData[symbol + TIME_SPANS[0].first + TIME_SPANS[0].second],
                        chartLoading = false)
                else getCurrentChartData(symbol)
            }
        }
    }

    private fun getCurrentChartData(symbol: String, pos: Int = 0) = viewModelScope.launch {
        val chartDataResult = async { alphaVantageRepository.getChartData(symbol, TIME_SPANS[pos]) }
        processNetworkResult(chartDataResult.await()) { chartData ->
            store.update {
                val map = it.chartData.toMutableMap()
                map[symbol + TIME_SPANS[pos].first + TIME_SPANS[pos].second] = chartData

                stateFlow.value = stateFlow.value.copy(
                    chartData = chartData.ifEmpty { null },
                    chartLoading = false,
                    stock = getUpdatedStock(map[symbol + TIME_SPANS[pos].first + TIME_SPANS[pos].second]!!)
                )
                it.copy(chartData = map)
            }
        }
    }

    private fun getStockSummary(symbol: String) = viewModelScope.launch {

        val stockSummaryResult = repository.makeNetworkCall(symbol) {
            yhFinanceApi.getStockSummary(symbol)
        }
        processNetworkResult(stockSummaryResult) { stockResponse ->
            store.update {
                val list = it.stockList.toMutableList()
                val updatedStock = stockResponse.toStock().copy(
                    isFavorite = it.favorites.contains(symbol)
                )
                list.removeIf{it.symbol == updatedStock.symbol}
                list.add(updatedStock)
                stateFlow.value = stateFlow.value.copy(
                    stock = updatedStock,
                    isLoading = false,
                    tabLoading = false
                )
                it.copy(stockList = list)
            }
        }
    }

    fun onEvent(event: InfoScreenEvent) {
        when (event) {
            is InfoScreenEvent.OnMarketOrderEvent -> {
                viewModelScope.launch {
                    store.stateFlow.value.portfolio?.let { portfolio ->
                        val transactions = portfolio.transactions.toMutableList()
                        transactions.add(event.transaction)

                        val updatedPortfolio = portfolio.copy(
                            transactions = transactions,
                            totalBalance = (portfolio.totalBalance + if(event.transaction.orderType == OrderType.Buy)
                                event.transaction.amount else - event.transaction.amount).toNDecimals(2)
                        )
                        repository.insertPortfolio(portfolio = updatedPortfolio)
                        store.update { appState -> appState.copy(portfolio = updatedPortfolio) }
                    }
                }
            }
            is InfoScreenEvent.OnRetry -> initSetup()
            is InfoScreenEvent.OnFavorite -> {
                event.stock?.let {
                    val updatedStock = it.copy(isFavorite = !it.isFavorite)
                    stateFlow.value = stateFlow.value.copy(stock = updatedStock)
                    viewModelScope.launch {
                        Log.i(TAG, "inserting: $updatedStock")
                        repository.insertStock(updatedStock)
                        store.update {
                            val list = it.favorites.toMutableList()
                            if (updatedStock.isFavorite) list.add(0, updatedStock.symbol)
                            else list.remove(updatedStock.symbol)
                            it.copy(favorites = list)
                        }
                    }
                }
            }
            is InfoScreenEvent.OnChangeChartTimeSpan -> {
                viewModelScope.launch {
                    stateFlow.value = stateFlow.value.copy(chartLoading = true)
                    store.read {
                        if (it.chartData.containsKey(symbol + TIME_SPANS[event.pos].first + TIME_SPANS[event.pos].second)) {
                            stateFlow.value = stateFlow.value.copy(
                                chartData = it.chartData[symbol + TIME_SPANS[event.pos].first + TIME_SPANS[event.pos].second],
                                chartLoading = false,
                                stock = if (event.pos == 0)
                                    it.stockList.find { it.symbol == symbol } else getUpdatedStock(
                                    it.chartData[symbol + TIME_SPANS[event.pos].first + TIME_SPANS[event.pos].second]!!
                                )
                            )

                        } else getCurrentChartData(symbol, event.pos)
                    }
                }
            }
            is InfoScreenEvent.OnTabSelected -> {
                viewModelScope.launch {
                    stateFlow.value = stateFlow.value.copy(tabLoading = true, tabNr = event.tabNr)
                    store.read {
                        when(event.tabNr) {
                            0 -> { stateFlow.value = stateFlow.value.copy(tabLoading = false) }
                            1 -> { stateFlow.value = stateFlow.value.copy(tabLoading = false) }
                            2 -> {
                                if (it.portfolio != null) {
                                    stateFlow.value = stateFlow.value.copy(
                                        transactions = it.portfolio.transactions.filter { it.symbol == symbol })
                                }
                                stateFlow.value = stateFlow.value.copy(tabLoading = false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getUpdatedStock(list: List<ChartData>): Stock? {
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
                    stateFlow.value = stateFlow.value.copy(
                            isLoading = false, tabLoading = false, chartLoading = false)
                }
            }
        }
    }

    data class InfoScreenStates(
        val stock: Stock? = null,
        val chartData: List<ChartData>? = null,
        val transactions: List<Transaction> = emptyList(),
        val isLoading: Boolean = false,
        val chartLoading: Boolean = false,
        val tabLoading: Boolean = false,
        val error: String? = null,
        val tabNr: Int = 0
    )
}

sealed class InfoScreenEvent {
    data class OnMarketOrderEvent(val transaction: Transaction) : InfoScreenEvent()
    object OnRetry : InfoScreenEvent()
    data class OnFavorite(val stock: Stock?) : InfoScreenEvent()
    data class OnChangeChartTimeSpan(val pos: Int): InfoScreenEvent()
    data class OnTabSelected(val tabNr: Int): InfoScreenEvent()
}


//private suspend fun getLocalStock(symbol: String, name: String): Stock {
//    var stock = repository.getLocalStockBySymbol(symbol)
//    if (stock != null)
//        return stock
//    stock = Stock(symbol = symbol, name = name)
//    repository.insertStock(stock)
//    return stock
//}


