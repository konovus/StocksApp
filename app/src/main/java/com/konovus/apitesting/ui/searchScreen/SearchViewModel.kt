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
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.data.repository.AlphaVantageRepository
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AlphaVantageRepository
) : ViewModel() {

    private val stateFlow = MutableStateFlow(SearchScreenState())
    val state: LiveData<SearchScreenState> = stateFlow.asLiveData()

    private val eventChannel = Channel<String>()
    val event = eventChannel.receiveAsFlow()

    private var searchJob: Job? = null

    private fun sendEvent(message: String) = viewModelScope.launch {
        eventChannel.send(message)
    }

    init {
        getCompanyListings()
    }

    fun onEvent(event: SearchScreenEvent) {
        when (event) {
            is SearchScreenEvent.Refresh -> {
                getCompanyListings(query = "")
            }
            is SearchScreenEvent.OnSearchQueryChange -> {
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

    fun getCompanyListings(
        query: String = stateFlow.value.searchQuery.lowercase(),
        forceRefresh: Boolean = false
        ) = viewModelScope.launch {
            stateFlow.update { it.copy(isLoading = true) }
            val result = repository.getCompanyListings(query)
            processNetworkResult(result) { pagingSource ->
                Log.i(TAG, "getCompanyListings: ")
                stateFlow.update { it.copy(
                        companies = Pager(
                            config = PagingConfig(
                                pageSize = 50,
                                maxSize = 300,
                                enablePlaceholders = false,
                            )
                        ) { pagingSource }.flow.cachedIn(viewModelScope))
                }
            }

    }

    private fun <T> processNetworkResult(
        result: Resource<T>,
        processBlock: suspend (T) -> Unit
    ) = viewModelScope.launch {
        when (result) {
            is Resource.Success -> {
                result.data?.let { processBlock(it) }
                stateFlow.update { it.copy(isLoading = false) }
            }
            is Resource.Loading -> stateFlow.update { it.copy(isLoading = true) }
            is Resource.Error -> { sendEvent(message = result.message.orEmpty()) }
        }
    }

    data class SearchScreenState(
        val companies: Flow<PagingData<CompanyInfo>> = emptyFlow(),
        val isLoading: Boolean = false,
        val searchQuery: String = "",
        val error: String? = null
    )
}

sealed class SearchScreenEvent {
    object Refresh : SearchScreenEvent()
    data class OnSearchQueryChange(val query: String) : SearchScreenEvent()
}