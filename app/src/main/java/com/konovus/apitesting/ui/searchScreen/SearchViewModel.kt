package com.konovus.apitesting.ui.searchScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.dao.CompanyDao
import com.konovus.apitesting.data.local.dao.PortfolioDao
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.repository.AlphaVantageRepository
import com.konovus.apitesting.util.Constants
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.NetworkStatus
import com.konovus.apitesting.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AlphaVantageRepository,
    private val store: Store<AppState>,
    private val companyDao: CompanyDao
) : ViewModel() {

    private val stateFlow = MutableStateFlow(SearchScreenState())
    val state: LiveData<SearchScreenState> = stateFlow.asLiveData()

    private var searchJob: Job? = null

    init {
        observeConnectivity()
        getCompanyListings()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            store.stateFlow.map { it.networkStatus }.distinctUntilChanged().collectLatest {
                if (it == NetworkStatus.BackOnline
                    && store.stateFlow.value.bottomNavSelectedId == R.id.searchFragment)
                    getCompanyListings()
            }
        }
    }


    fun onEvent(event: SearchScreenEvent) {
        when (event) {
            is SearchScreenEvent.Refresh -> {
                getCompanyListings(fetchFromRemote = true)
            }
            is SearchScreenEvent.OnSearchQueryChange -> {
                Log.i(TAG, "onEvent: SearchQuery")
                if (event.query == stateFlow.value.searchQuery) return
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    stateFlow.value = stateFlow.value.copy(searchQuery = event.query)
                    getCompanyListings()
                }
            }
        }
    }

    private fun getCompanyListings(
        query: String = stateFlow.value.searchQuery.lowercase(),
        fetchFromRemote: Boolean = false
    ) {
        viewModelScope.launch {
            stateFlow.value = stateFlow.value.copy(isLoading = true)
            repository
                .getCompanyListings(fetchFromRemote, query)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { _ ->
                                stateFlow.value = stateFlow.value.copy(
                                    //todo
                                    companies = Pager(
                                        config = PagingConfig(
                                            pageSize = 50,
                                            maxSize = 300,
                                            enablePlaceholders = false,
                                        )
                                    ) { companyDao.searchCompanyInfoPaged(query) }
                                        .flow.cachedIn(viewModelScope),
                                    isLoading = false
                                )
                            }
                        }
                        is Resource.Loading -> {
                            stateFlow.value = stateFlow.value.copy(isLoading = result.isLoading)
                        }
                        is Resource.Error -> {
                            stateFlow.value = stateFlow.value.copy(error = result.message)
                        }
                    }

                }
        }
    }

    fun updateBottomNavSelectedId(id: Int) {
        viewModelScope.launch {
            store.update {
                it.copy(bottomNavSelectedId = id)
            }
        }
    }

    data class SearchScreenState(
        val companies: Flow<PagingData<CompanyInfo>> = emptyFlow(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val searchQuery: String = "",
        val error: String? = null
    )
}

sealed class SearchScreenEvent {
    object Refresh : SearchScreenEvent()
    data class OnSearchQueryChange(val query: String) : SearchScreenEvent()
}