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
import com.konovus.apitesting.data.local.entities.Portfolio
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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

    init {
        observeConnectivity()
        if (store.stateFlow.value.networkStatus == NetworkStatus.Available)
            initSetup()
    }

    private fun initSetup() {
        getOrCreateDefaultPortfolio()
        getTrendingStocks()
        getFavoritesNr()
        getFavoritesStocksFromDb()
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
                stateFlow.value = stateFlow.value.copy(error = responseMultipleQuotes.message)
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
                stateFlow.value = stateFlow.value.copy(error = responseChartData.message)
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

    private fun getTrendingStocks() {
        viewModelScope.launch {
            stateFlow.value = stateFlow.value.copy(trendingLoading = true)
            val result = repository.makeNetworkCall("trending") {
                yhFinanceApi.getTrendingStocks()
            }
            processNetworkResult(result) { data ->
                store.update { appState ->
                    appState.copy(
                        trendingStocks = data.finance.result.first().quotes.map { it.toStock() }
                            .filter { it.quoteType == "EQUITY" }
                    )
                }
            }
            stateFlow.value = stateFlow.value.copy(trendingLoading = false)
        }
    }


    fun onEvent(event: MainScreenEvents) {
        when (event) {
            is MainScreenEvents.OnRequestPortfolioUpdate -> viewModelScope.launch {
                //todo
                stateFlow.value = stateFlow.value.copy(isUpdatingPortfolio = true)
                repository.updatePortfolioStocksPrices(event.portfolio)
                stateFlow.value = stateFlow.value.copy(isUpdatingPortfolio = false)
            }
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
                is Resource.Loading -> stateFlow.value = stateFlow.value.copy(isLoading = true)
                is Resource.Error -> {
                    stateFlow.value =
                        stateFlow.value.copy(error = result.message, isLoading = false)
                }
            }
        }
    }

    private fun getOrCreateDefaultPortfolio() = viewModelScope.launch {
        if (store.stateFlow.value.portfolio == null)
            repository.getPortfolioById(1)?.let { portfolio ->
            store.update { it.copy(portfolio = portfolio) }
        } ?: run {
            val portfolio = Portfolio(name = "Default", id = 1)
            store.update { it.copy(portfolio = portfolio) }
            repository.insertPortfolio(portfolio)
        }
    }

    fun clearError() {
        stateFlow.value = stateFlow.value.copy(error = null)
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
}


sealed class MainScreenEvents {
    data class OnRequestPortfolioUpdate(val portfolio: Portfolio): MainScreenEvents()
}


