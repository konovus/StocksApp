package com.konovus.apitesting.ui.mainScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.R
import com.konovus.apitesting.data.api.FinageApi
import com.konovus.apitesting.data.local.entities.IntraDayInfo
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.repository.MainRepository
import com.konovus.apitesting.util.NetworkStatus
import com.konovus.apitesting.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val finageApi: FinageApi,
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
        getFavoritesStocks()
        getTrendingStocks()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            store.stateFlow.map { it.networkStatus }.distinctUntilChanged().collectLatest {
                if (it == NetworkStatus.BackOnline
                    && store.stateFlow.value.bottomNavSelectedId == R.id.mainFragment)
                    initSetup()
            }
        }
    }

    private fun getFavoritesStocks() {
        repository.getFavoritesFlow().onEach {
            stateFlow.value = stateFlow.value.copy(favoritesList = it)
        }.launchIn(viewModelScope)
    }

    private fun getTrendingStocks() {
        viewModelScope.launch {
            stateFlow.value = stateFlow.value.copy(trendingLoading = true)
            val result = repository.makeNetworkCall("trending") {
                finageApi.getMostActive()
            }
            processNetworkResult(result) { data ->
                store.update { appState -> appState.copy(trendingStocks = data.map { it.toStock() }) }
                stateFlow.value = stateFlow.value.copy(trendingLoading = false)
            }
        }
    }

    fun updatePricesForFavorites(localList: List<Stock>) {
        viewModelScope.launch {
            stateFlow.value = stateFlow.value.copy(favoritesLoading = true)
            val result = repository.makeNetworkCall("favorites") {
                finageApi.getMultipleStocks(localList.joinToString(",") { it.symbol })
            }
            if (result.data == null) return@launch
            val updatedList = localList.map { stock ->
                stock.copy(price = result.data.find { it?.symbol == stock.symbol }?.ask ?: 0.0,
                    priceLastUpdated = System.currentTimeMillis())
            }
            repository.insertStocks(updatedList)
            stateFlow.value = stateFlow.value.copy(favoritesLoading = false)
        }
    }

    fun onEvent(event: MainScreenEvents) {
        when (event) {
            is MainScreenEvents.OnRequestPortfolioUpdate -> {
                viewModelScope.launch {
                    repository.updatePortfolioStocksPrices(event.portfolio)
                }
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

    private fun getOrCreateDefaultPortfolio() {
        viewModelScope.launch {
            if (store.stateFlow.value.portfolio == null)
            repository.getPortfolioById(1)?.let { portfolio ->
                store.update { it.copy(portfolio = portfolio) }
            } ?: run {
                val portfolio = Portfolio(name = "Default", id = 1)
                store.update { it.copy(portfolio = portfolio) }
                repository.insertPortfolio(portfolio)
            }
        }
    }

    fun clearError() {
        stateFlow.value = stateFlow.value.copy(error = null)
    }

    data class MainScreenStates(
        val stockInfoData: MutableMap<String, List<IntraDayInfo>> = mutableMapOf(),
        val trendingCompanies: List<Stock> = emptyList(),
        val favoritesList: List<Stock> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val trendingLoading: Boolean = false,
        val favoritesLoading: Boolean = false
    )
}


sealed class MainScreenEvents {
    data class OnRequestPortfolioUpdate(val portfolio: Portfolio): MainScreenEvents()
}


