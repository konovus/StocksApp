package com.konovus.apitesting.ui.mainScreen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.konovus.apitesting.R
import com.konovus.apitesting.TrendingItemBindingModelBuilder
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.models.FavoritesUiModel
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.databinding.MainFragmentBinding
import com.konovus.apitesting.trendingItem
import com.konovus.apitesting.ui.MainActivity
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Constants.TEN_MINUTES
import com.konovus.apitesting.util.Constants.TIME_SPANS
import com.konovus.apitesting.util.NetworkConnectionObserver
import com.konovus.apitesting.util.NetworkStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject


@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment), FavoritesAdapter.OnItemClickListener {

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private var refreshData: Job? = null

    @Inject
    lateinit var networkConnectionObserver: NetworkConnectionObserver

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = MainFragmentBinding.bind(view)

        observeNetworkConnectivity()
        bindPortfolioData()
        bindFavoritesData()
        bindTrendingData()
        bindErrorHandling()
        setupListeners()
        keepDataUpdated()
    }

    private fun observeNetworkConnectivity() {
        networkConnectionObserver.connection.observe(viewLifecycleOwner) {
            if (it == NetworkStatus.Available || it == NetworkStatus.BackOnline)
                viewModel.initSetup()
        }
    }

    private fun keepDataUpdated() {
        refreshData?.cancel()
        refreshData = lifecycleScope.launchWhenStarted {
                while (true) {
                    viewModel.updateFavoritesQuotes()
                    viewModel.updateFavoritesChartData()
                    viewModel.updatePortfolio()
                    delay(TEN_MINUTES.toLong())
                }
            }
    }

    private fun bindFavoritesData() {
        viewModel.favoritesState.distinctUntilChanged().observe(viewLifecycleOwner) {
            viewModel.updateFavoritesQuotes()
            viewModel.updateFavoritesChartData()
            setupVisibilityAndLoadingStates(it)
            setupFavoritesRecyclerView(it)
        }
    }

    private fun setupFavoritesRecyclerView(state: MainViewModel.FavoritesUiState) {
        if (state.quotes.isNullOrEmpty() || state.chartData.isNullOrEmpty())
            return
        val favoritesAdapter = FavoritesAdapter(this@MainFragment)
        binding.recyclerViewFavorites.adapter = favoritesAdapter
        favoritesAdapter.submitList(state.quotes.map { quote ->
            FavoritesUiModel(
                quote = quote,
                chartData = state.chartData[quote.symbol + TIME_SPANS[0].first + TIME_SPANS[0].second]
                    ?: emptyList()
            )
        })
    }

    private fun setupVisibilityAndLoadingStates(state: MainViewModel.FavoritesUiState) = binding.apply {
        addStocksTv.isVisible = !state.hasFavorites
        addStocksBtn.isVisible = !state.hasFavorites

        favoritesShimmerLayout.root.isVisible = state.hasFavorites && (state.quotes.isNullOrEmpty() || state.chartData.isNullOrEmpty())
        recyclerViewFavorites.isVisible = !state.quotes.isNullOrEmpty() && !state.chartData.isNullOrEmpty()
        if (!state.isFetchingQuotes && !state.isFetchingChartData)
            favoritesShimmerLayout.root.stopShimmer()
        else favoritesShimmerLayout.root.startShimmer()
    }

    private fun bindTrendingData() = binding.apply {
        viewModel.trendingStocks.observe(viewLifecycleOwner) { list ->
            recyclerViewTrending.withModels {
                list.forEach {
                    trendingItem { setupEachTrendingItem(it) }
                }
            }
            trendingShimmerLayout.root.isVisible = list.isEmpty()
            recyclerViewTrending.isVisible = list.isNotEmpty()
            if (list.isEmpty())
                trendingShimmerLayout.root.startShimmer()
            else trendingShimmerLayout.root.stopShimmer()
        }
    }

    private fun TrendingItemBindingModelBuilder.setupEachTrendingItem(stock: Stock) {
        id(stock.symbol)
        stock(stock)
        onClick { _ ->
            val action = MainFragmentDirections.actionMainFragmentToInfoFragment(stock.name, stock.symbol)
            findNavController().navigate(action)
        }
    }

    private fun bindPortfolioData()  {
        viewModel.portfolio.distinctUntilChanged().observe(viewLifecycleOwner) {
            Log.i(TAG, "bindPortfolioData: ${it.stocksToShareAmount}")
            binding.portfolio = it
            viewModel.updatePortfolio()
        }
    }


    private fun bindErrorHandling() {
        viewModel.event.asLiveData().observe(viewLifecycleOwner) { message ->
            Log.i(TAG, "bindErrorHandling: $message")
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
    }

    private fun setupListeners() {
        (activity as? MainActivity)?.navigateToTab(R.id.mainFragment)
        binding.addStocksBtn.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.searchFragment)
        }
    }

    override fun onFavoriteItemClick(quote: Quote) {
        val action = MainFragmentDirections.actionMainFragmentToInfoFragment(quote.name, quote.symbol)
        findNavController().navigate(action)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}
