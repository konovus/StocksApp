package com.konovus.apitesting.ui.mainScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.R
import com.konovus.apitesting.data.api.YhFinanceApi
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.repository.MainRepository
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Constants.TEN_MINUTES
import com.konovus.apitesting.util.Constants.TIME_SPANS
import com.konovus.apitesting.util.NetworkStatus
import com.konovus.apitesting.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val yhFinanceApi: YhFinanceApi,
    val store: Store<AppState>,
    app: Application
) : AndroidViewModel(app) {

    private val stateFlow = MutableStateFlow(MainScreenStates())
    val state: LiveData<MainScreenStates> = stateFlow.asLiveData()

    val favorites = repository.getFavoritesFlow().map { list ->
        store.update { it.copy(favorites = list.map { it.symbol }) }
        list.sortedByDescending { it.id }
    }

    private val _trendingStocks = MutableStateFlow(emptyList<Stock>())
    val trendingStocks: LiveData<List<Stock>> = _trendingStocks.asLiveData()

    private val eventChannel = Channel<String>()
    val event = eventChannel.receiveAsFlow()

    init {
        observeConnectivity()
        if (store.stateFlow.value.networkStatus == NetworkStatus.Available)
            initSetup()
    }

    private fun initSetup() {
        getDefaultPortfolio()
//        updatePortfolioStocksPrices()
        getTrendingStocks()
        getFavoritesNr()
//        getFavoritesStocksFromDb()
    }

    private fun sendEvent(message: String) = viewModelScope.launch {
        eventChannel.send(message)
    }

    private fun updatePortfolioStocksPrices() = viewModelScope.launch {
        while (true) {
            if (store.stateFlow.value.portfolio != null &&
                store.stateFlow.value.portfolio!!.lastUpdatedTime + TEN_MINUTES < System.currentTimeMillis())
                repository.updatePortfolioStocksPrices(store.stateFlow.value.portfolio!!)
            delay(TEN_MINUTES.toLong())
        }
    }

    private fun getFavoritesNr() = viewModelScope.launch {
        val favoritesNr = repository.getFavoritesNr()
        stateFlow.value = stateFlow.value.copy(
            favoritesNr = favoritesNr,
            favoritesLoading = favoritesNr > 0
        )
    }

    private fun observeConnectivity() = viewModelScope.launch {
        store.stateFlow.map { it.networkStatus }.distinctUntilChanged().collectLatest {
            if (it == NetworkStatus.BackOnline
                && store.stateFlow.value.bottomNavSelectedId == R.id.mainFragment) {
                initSetup()
            }
        }
    }

    fun updateFavoritesQuotes(localFavs: List<Stock>) = viewModelScope.launch {
        if (localFavs.minOf { it.lastUpdatedTime } + TEN_MINUTES < System.currentTimeMillis()
            && !stateFlow.value.isUpdatingQuotes) {
            stateFlow.value = stateFlow.value.copy(favoritesLoading = true, isUpdatingQuotes = true)
            val responseMultipleQuotes = repository.makeNetworkCall(
                "quotes ${localFavs.joinToString(",") { it.symbol }}") {
                yhFinanceApi.getMultipleQuotes(localFavs.joinToString(",") { it.symbol })
            }
            if (responseMultipleQuotes is Resource.Success) {
                val updatedStocks = responseMultipleQuotes.data!!.quoteResponse.result.map { result ->
                    result.toStock().copy(isFavorite = true, id = localFavs.find { it.symbol == result.symbol }!!.id)
                }
                Log.i(TAG, "updateFavoritesQuotes: ${updatedStocks.map { Pair(it.symbol, it.lastUpdatedTime) }}")
                repository.updateStocks(stocks = updatedStocks)
            } else {
                sendEvent(message = responseMultipleQuotes.message.orEmpty())
            }
            stateFlow.value = stateFlow.value.copy(favoritesLoading = false, isUpdatingQuotes = false)
        }
    }

    fun updateFavoritesChartData(localFavs: List<Stock>) = viewModelScope.launch {
        if (!store.stateFlow.value.chartData.map { it.key }.containsAll(localFavs.map {
                it.symbol + TIME_SPANS[0].first + TIME_SPANS[0].second })
            && !stateFlow.value.isUpdatingChartsData) {
            stateFlow.value = stateFlow.value.copy(favoritesLoading = true, isUpdatingChartsData = true)
            val responseChartData = repository.makeNetworkCall(
                "chartData ${localFavs.joinToString(",") { it.symbol }}"
            ) {
                yhFinanceApi.getMultipleChartsData(localFavs.joinToString(",") { it.symbol })
            }
            if (responseChartData is Resource.Success) {
                store.update { appState ->
                    val map = appState.chartData.toMutableMap()
                    responseChartData.data!!.values.forEach {
                        map[it.symbol + TIME_SPANS[0].first + TIME_SPANS[0].second] =
                            it.close.mapIndexed { index, close ->  ChartData(
                                close = close,
                                timestamp = it.timestamp[index].toString()
                            ) }
                    }
                    appState.copy(chartData = map)
                }
            } else {
                sendEvent(message = responseChartData.message.orEmpty())
            }
            stateFlow.value = stateFlow.value.copy(favoritesLoading = false, isUpdatingChartsData = false)
        }
    }

    private fun getFavoritesStocksFromDb() = viewModelScope.launch {
        repository.getFavoritesFlow().collectLatest { favorites ->
            Log.i(TAG, "Main VM getFavoritesStocksFromDb: ${favorites.map { it.symbol }}")
            stateFlow.value = stateFlow.value.copy(
                favoritesList = favorites.sortedByDescending { it.id },
                favoritesNr = favorites.size
            )
            store.update { it.copy(favorites = favorites.map { it.symbol }) }
        }
    }

    private fun getTrendingStocks() = viewModelScope.launch {
        val response = repository.makeNetworkCall("trending") { yhFinanceApi.getTrendingStocks() }
        if (response is Resource.Success && response.data != null) {
            val stocks = response.data.finance.result.first().quotes.map { it.toStock() }
                .filter { it.quoteType == "EQUITY" }
            if (stocks.isEmpty()) sendEvent("No trending stocks.")
            _trendingStocks.value = stocks
        } else if (response is Resource.Error)
            sendEvent(response.message.toString().ifEmpty { "Unexpected error occurred." })
    }

//    private fun getTrendingStocks() = viewModelScope.launch {
//        stateFlow.value = stateFlow.value.copy(trendingLoading = true)
//        val result = repository.makeNetworkCall("trending") {
//            yhFinanceApi.getTrendingStocks()
//        }
//        processNetworkResult(result) { data ->
//            val stocks = data.finance.result.first().quotes.map { it.toStock() }
//                .filter { it.quoteType == "EQUITY" }
//            if (stocks.isEmpty()) return@processNetworkResult
//            store.update { appState ->
//                appState.copy(
//                    trendingStocks = stocks
//                )
//            }
//        }
//        stateFlow.value = stateFlow.value.copy(trendingLoading = false)
//    }

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
                    sendEvent(message = result.message.orEmpty())
                }
            }
        }
    }

    private fun getDefaultPortfolio() = viewModelScope.launch {
        repository.getPortfoliosFlow().collectLatest { list ->
            Log.i(TAG, "getDefaultPortfolio: $list")
            if (list.isEmpty()) return@collectLatest
            store.update { it.copy(portfolio = list.first()) }
        }
    }

    data class MainScreenStates(
        val stockInfoData: MutableMap<String, List<ChartData>> = mutableMapOf(),
        val trendingCompanies: List<Stock> = emptyList(),
        val favoritesList: List<Stock> = emptyList(),
        val favoritesNr: Int? = null,
        val isUpdatingChartsData: Boolean = false,
        val isUpdatingQuotes: Boolean = false,
        val isUpdatingPortfolio: Boolean = false,
        val isLoading: Boolean = false,
        val trendingLoading: Boolean = false,
        val favoritesLoading: Boolean = false,
        val error: String? = null,
    )

    sealed class Event{
        data class ErrorEvent(val message: String): Event()
    }
}


