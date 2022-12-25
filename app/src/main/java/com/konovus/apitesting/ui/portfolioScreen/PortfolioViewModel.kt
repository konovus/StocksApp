package com.konovus.apitesting.ui.portfolioScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.data.repository.IMainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val repository: IMainRepository
) : ViewModel() {

    val state = repository.getProfileFlow().map { profile ->
        PortfolioScreenState(
            portfolio = profile.portfolio,
            quotes = profile.portfolio.stocksToShareAmount.keys.mapNotNull { symbol ->
                repository.portfolioQuotesCache.find { it.symbol == symbol } ?:
                repository.getStock(symbol)?.toQuote()
            })
    }.asLiveData()

    data class PortfolioScreenState(
        val portfolio: Portfolio? = null,
        val quotes: List<Quote> = emptyList(),
    )
}