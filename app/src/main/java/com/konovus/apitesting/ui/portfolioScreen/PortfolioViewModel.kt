package com.konovus.apitesting.ui.portfolioScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.repository.MainRepository
import com.konovus.apitesting.util.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val repository: MainRepository,
    val store: Store<AppState>
) : ViewModel() {

    private var stateFlow = MutableStateFlow(PortfolioScreenState())
    val state = stateFlow.asLiveData()

    init {
        observeConnectivity()
        initSetup()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            store.stateFlow.map { it.networkStatus }.distinctUntilChanged().collectLatest {
                if (it == NetworkStatus.BackOnline
                    && store.stateFlow.value.bottomNavSelectedId == R.id.portfolioFragment)
                    initSetup()
            }
        }
    }

    private fun initSetup() {
        viewModelScope.launch {
            store.stateFlow.map{ it.portfolio }.filterNotNull().collectLatest { portfolio ->
                val stocks = portfolio.stocksToShareAmount.mapNotNull {
                    repository.getLocalStockBySymbol(it.key)
                }
                stateFlow.value = stateFlow.value.copy(portfolio = portfolio, stocks = stocks)
            }
        }
    }

    fun requestPortfolioUpdate(portfolio: Portfolio) {
        viewModelScope.launch {
            repository.updatePortfolioStocksPrices(portfolio)
        }
    }

    fun clearError() {
        stateFlow.value = stateFlow.value.copy(error = null)
    }

    data class PortfolioScreenState(
        val isLoading: Boolean = false,
        val portfolio: Portfolio? = null,
        val stocks: List<Stock> = emptyList(),
        val error: String? = null
    )
}