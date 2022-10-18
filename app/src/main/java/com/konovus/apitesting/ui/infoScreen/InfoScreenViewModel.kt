package com.konovus.apitesting.ui.infoScreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.data.api.AlphaVantageApi
import com.konovus.apitesting.data.api.FinageApi
import com.konovus.apitesting.data.local.entities.IntraDayInfo
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.entities.Transaction
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.remote.responses.QuoteResponse
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val alphaVantageRepository: AlphaVantageRepository,
    private val repository: MainRepository,
    private val finageApi: FinageApi,
    private val alphaVantageApi: AlphaVantageApi,
    val store: Store<AppState>
) : ViewModel() {

    private var stateFlow = MutableStateFlow(InfoScreenStates())
    val state = stateFlow.asLiveData()

    val symbol = savedStateHandle.get<String>("symbol")!!

    init {
        observeConnectivity()
        initSetup()
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
            val stock = getLocalStock(symbol, name = savedStateHandle.get<String>("name")!!)
            stateFlow.value = stateFlow.value.copy(stock = stock)
            store.read {
                if (it.quoteList.containsKey(symbol)
                    && it.quoteList[symbol]!!.lastUpdatedTime + TEN_MINUTES < System.currentTimeMillis())
                    stateFlow.value =
                        stateFlow.value.copy(quote = it.quoteList[symbol]!!, isLoading = false)
                else getCurrentQuote(symbol)

                if (it.chartData.containsKey(symbol + TIME_SPANS[0].first + TIME_SPANS[0].second)
                    && it.chartData[symbol + TIME_SPANS[0].first + TIME_SPANS[0].second]!!.first().lastUpdatedTime
                    + TEN_MINUTES < System.currentTimeMillis())
                    stateFlow.value =
                        stateFlow.value.copy(chartData = it.chartData[symbol + TIME_SPANS[0].first + TIME_SPANS[0].second],
                            chartLoading = false)
                else getCurrentChartData(symbol)
            }
        }
    }

    private suspend fun getLocalStock(symbol: String, name: String): Stock {
        var stock = repository.getLocalStockBySymbol(symbol)
        if (stock != null)
            return stock
        stock = Stock(symbol = symbol, name = name)
        repository.insertStock(stock)
        return stock
    }

    private fun getCurrentChartData(symbol: String, pos: Int = 0) {
        viewModelScope.launch {
            Log.i(TAG, "getCurrentChartData: VM")
            val intradayInfoResult = async { alphaVantageRepository.getIntradayInfo(symbol, TIME_SPANS[pos]) }
            processNetworkResult(intradayInfoResult.await()) { intraDayList ->
                store.update {
                    val map = it.chartData.toMutableMap()
                    map[symbol + TIME_SPANS[pos].first + TIME_SPANS[pos].second] = intraDayList
                    it.copy(chartData = map)
                }
                if (pos != 0)
                    stateFlow.value = stateFlow.value.copy(
                        chartData = intraDayList.ifEmpty { null },
                        chartLoading = false,
                        quote = getUpdatedQuote(intraDayList)
                    )
                else
                    stateFlow.value = stateFlow.value.copy(
                        chartData = intraDayList.ifEmpty { null },
                        chartLoading = false,
                        quote = store.stateFlow.value.quoteList[symbol]
                    )
            }
        }
    }

    private fun getCurrentQuote(symbol: String) {
        viewModelScope.launch {
            val quoteResult = repository.makeNetworkCall(symbol) {
                alphaVantageApi.getQuote(symbol)
            }
            processNetworkResult(quoteResult) { quoteResponse ->
                if (quoteResponse.globalQuote == null)
                    return@processNetworkResult
                store.update {
                    val map = it.quoteList.toMutableMap()
                    map[symbol] = quoteResponse.globalQuote
                    it.copy(quoteList = map)
                }
//                repository.updateStock(
//                    getLocalStock(symbol, "")
//                        .copy(price = quoteResponse.globalQuote.price.toDouble().toNDecimals(2))
//                )
                stateFlow.value = stateFlow.value.copy(
                    quote = quoteResponse.globalQuote,
                    isLoading = false,
                    tabLoading = false
                )
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
                        stateFlow.value.stock?.let { repository.insertStock(it.copy(price = event.transaction.price)) }
                    }
                }
            }
            is InfoScreenEvent.OnRetry -> initSetup()
            InfoScreenEvent.OnFavorite -> {
                stateFlow.value.stock?.let {
                    stateFlow.value = stateFlow.value.copy(
                        stock = it.copy(isFavorite = !it.isFavorite)
                    )
                    viewModelScope.launch { repository.updateStock(stateFlow.value.stock!!) }
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
                                quote = if (event.pos == 0) it.quoteList[symbol] else getUpdatedQuote(
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
                    Log.i(TAG, "onEvent tabSelected ${event.tabNr}: ${stateFlow.value.tabLoading}")
                    store.read {
                        when(event.tabNr) {
                            0 -> {
                                if (it.quoteList.containsKey(symbol))
                                    stateFlow.value = stateFlow.value.copy(
                                        quote = it.quoteList[symbol],
                                        tabLoading = false)
                                else getCurrentQuote(symbol)

                            }
                            1 -> { if (it.detailsStocks.map { it.symbol }.contains(symbol)) {
                                    stateFlow.value = stateFlow.value.copy(
                                        detailsStock = it.detailsStocks.find { it.symbol == symbol },
                                        tabLoading = false
                                    )
                                } else getDetailsStock(symbol)
                            }
                            2 -> {
                                if (it.portfolio != null) {
                                    stateFlow.value = stateFlow.value.copy(tabLoading = false,
                                        transactions = it.portfolio.transactions.filter { it.symbol == symbol })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDetailsStock(symbol: String) {
        viewModelScope.launch {
            val response = repository.makeNetworkCall("$symbol details") {
                finageApi.getStockDetails(symbol)
            }
            processNetworkResult(response) {
                store.update { appState ->
                    val list = appState.detailsStocks.toMutableList()
                    list.add(it.toStock())
                    appState.copy(detailsStocks = list)
                }
                stateFlow.value = stateFlow.value.copy(
                    detailsStock = it.toStock(),
                    tabLoading = false
                )
                Log.i(TAG, "getDetailsStock $symbol: ${it.toStock()}")
            }
        }
    }

    private fun getUpdatedQuote(list: List<IntraDayInfo>): QuoteResponse.GlobalQuote? {
        val change = (list.last().close - list.first().close).toNDecimals(2)
        val changeInPercent = (change / list.first().close * 100).toNDecimals(2)
        return stateFlow.value.quote?.copy(
            change = change.toString(), changePercent = changeInPercent.toString()
        )
    }

    fun clearError() {
        stateFlow.value = stateFlow.value.copy(error = null)
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
                is Resource.Loading -> stateFlow.value = stateFlow.value.copy(isLoading = true)
                is Resource.Error -> {
                    stateFlow.value =
                        stateFlow.value.copy(error = result.message,
                            isLoading = false, tabLoading = false, chartLoading = false)
                }
            }
        }
    }

    data class InfoScreenStates(
        val stock: Stock? = null,
        val quote: QuoteResponse.GlobalQuote? = null,
        val chartData: List<IntraDayInfo>? = null,
        val detailsStock: Stock? = null,
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
    object OnFavorite : InfoScreenEvent()
    data class OnChangeChartTimeSpan(val pos: Int): InfoScreenEvent()
    data class OnTabSelected(val tabNr: Int): InfoScreenEvent()
}


