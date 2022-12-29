package com.konovus.apitesting.ui.infoScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.data.local.entities.*
import com.konovus.apitesting.data.repository.AlphaVantageRepository
import com.konovus.apitesting.data.repository.IMainRepository
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
class InfoScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alphaVantageRepository: AlphaVantageRepository,
    private val repository: IMainRepository,
) : ViewModel() {

    private val symbol = savedStateHandle.get<String>("symbol")!!

    val profile: Flow<Profile> = repository.getProfileFlow().filterNotNull().map { profile ->
        stateFlow.update { it.copy(
            profile = profile,
            isFavorite = profile.favorites.contains(symbol)) }
        profile
    }

    private var stateFlow = MutableStateFlow(InfoScreenStates())
    val state = stateFlow.asLiveData()

    private val eventChannel = Channel<String>()
    val event = eventChannel.receiveAsFlow()


    private fun sendEvent(message: String) = viewModelScope.launch {
        eventChannel.send(message)
    }

    fun initSetup() {
        val cacheHasStock = repository.stocksCache.map { it.symbol }.contains(symbol)
        val stockIsUpdated = (repository.stocksCache.find { it.symbol == symbol }?.lastUpdatedTime ?: 0) + TEN_MINUTES > System.currentTimeMillis()
        if (cacheHasStock && stockIsUpdated)
            stateFlow.update { it.copy(stock = repository.stocksCache.find { it.symbol == symbol }!!) }
        else getStockSummary(symbol)

        val key = symbol + TIME_SPANS[0].first + TIME_SPANS[0].second
        if (repository.chartDataCache.containsKey(key))
            stateFlow.update { it.copy(chartData = repository.chartDataCache[key]) }
        else getCurrentChartData(symbol)
    }

    private fun getCurrentChartData(symbol: String, pos: Int = 0) = viewModelScope.launch {
        stateFlow.update { it.copy(chartLoading = true) }
        val chartDataResult = alphaVantageRepository.getChartData(symbol, TIME_SPANS[pos])
        processNetworkResult(chartDataResult) { chartData ->
            if (chartData.isEmpty()) return@processNetworkResult
            stateFlow.update { it.copy(
                chartData = chartData,
                chartLoading = false,
                stock = updateStockChartChange(pos)
            ) }
            repository.updateChartDataCache(symbol + TIME_SPANS[pos].first + TIME_SPANS[pos].second, chartData)
        }
    }

    private fun getStockSummary(symbol: String) = viewModelScope.launch {
        stateFlow.update { it.copy(isLoading = true) }
        val stockSummaryResult = repository.getStockSummary(symbol)
        processNetworkResult(stockSummaryResult) { stockResponse ->
            stateFlow.update { it.copy(
                stock = stockResponse.toStock(),
                isLoading = false
            ) }
            repository.updateStocksCache(stockResponse.toStock())
        }
    }

    fun onEvent(event: InfoScreenEvent) {
        when (event) {
            is InfoScreenEvent.OnMarketOrderEvent -> viewModelScope.launch {
                    val transactions = stateFlow.value.profile!!.portfolio.transactions.toMutableList()
                    transactions.add(event.transaction)

                    val updatedPortfolio = stateFlow.value.profile!!.portfolio.copy(
                        transactions = transactions,
                        totalBalance = (stateFlow.value.profile!!.portfolio.totalBalance + if (event.transaction.orderType == OrderType.Buy)
                            event.transaction.amount else - event.transaction.amount).toNDecimals(2)
                    )
                    if (updatedPortfolio.stocksToShareAmount.containsKey(symbol))
                        stateFlow.value.stock?.let { repository.updatePortfolioStocksCache(it.toQuote()) }
                    else repository.removeFromPortfolioStocksCache(symbol)
                    repository.updateProfile(profile = stateFlow.value.profile!!.copy(portfolio = updatedPortfolio))
            }
            is InfoScreenEvent.OnRetry -> initSetup()
            is InfoScreenEvent.OnFavorite -> {
                viewModelScope.launch {
                    repository.updateProfile(stateFlow.value.profile!!.updateFavorites(symbol))
                    repository.updateFavoritesCacheQuote(event.stock.toQuote())
                }
            }
            is InfoScreenEvent.OnChangeChartTimeSpan -> {
                viewModelScope.launch {
                    stateFlow.update { it.copy(chartLoading = true) }
                    val key = symbol + TIME_SPANS[event.pos].first + TIME_SPANS[event.pos].second
                    if (repository.chartDataCache.containsKey(key)) {
                        stateFlow.update { it.copy(
                            chartData = repository.chartDataCache[key],
                            stock = updateStockChartChange(event.pos),
                            chartLoading = false
                        ) }
                    } else getCurrentChartData(symbol, event.pos)
                }
            }
            is InfoScreenEvent.OnChangeTab -> stateFlow.update { it.copy(tabNr = event.nr) }
        }
    }

    private fun updateStockChartChange(pos: Int): Stock? {
        if (pos == 0)
            return repository.stocksCache.find { it.symbol == symbol }
        val list = repository.chartDataCache[symbol + TIME_SPANS[pos].first + TIME_SPANS[pos].second]!!
        val change = (list.last().close - list.first().close).toNDecimals(2)
        val changeInPercent = (change / list.first().close * 100).toNDecimals(2)
        return stateFlow.value.stock?.copy(
            chartChange = Stock.ChartChange(change,changeInPercent)
        )
    }

    private fun <T> processNetworkResult(
        result: Resource<T>,
        processBlock: suspend (T) -> Unit
    ) = viewModelScope.launch {
        when (result) {
            is Resource.Success -> processBlock(result.data!!)
            is Resource.Loading -> stateFlow.update { it.copy(isLoading = true) }
            is Resource.Error -> {
                sendEvent(message = result.message.orEmpty())
                stateFlow.update { it.copy(isLoading = false, chartLoading = false) }
            }
        }
    }

    data class InfoScreenStates(
        val stock: Stock? = null,
        val chartData: List<ChartData>? = null,
        val profile: Profile? = null,
        val isFavorite: Boolean = false,
        val isLoading: Boolean = false,
        val chartLoading: Boolean = false,
        val tabNr: Int = 0
    )
}

sealed class InfoScreenEvent {
    data class OnMarketOrderEvent(val transaction: Transaction) : InfoScreenEvent()
    object OnRetry : InfoScreenEvent()
    data class OnFavorite(val stock: Stock) : InfoScreenEvent()
    data class OnChangeChartTimeSpan(val pos: Int): InfoScreenEvent()
    data class OnChangeTab(val nr: Int): InfoScreenEvent()
}
