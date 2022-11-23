package com.konovus.apitesting.ui.portfolioScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.models.Quote
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

    private fun initSetup() = viewModelScope.launch {
        repository.getPortfoliosFlow().collectLatest { portfolios ->
            stateFlow.update { it.copy(
                portfolio = portfolios.first(),
                quotes = portfolios.first().stocksToShareAmount.keys.map { symbol ->
                    repository.portfolioQuotesCache.find { it.symbol == symbol } ?: Quote()
                }
            )}
        }
    }

    fun clearError() {
        stateFlow.value = stateFlow.value.copy(error = null)
    }

    data class PortfolioScreenState(
        val portfolio: Portfolio? = null,
        val quotes: List<Quote> = emptyList(),
        val error: String? = null
    )
}