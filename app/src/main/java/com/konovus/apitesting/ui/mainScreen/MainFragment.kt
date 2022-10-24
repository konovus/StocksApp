package com.konovus.apitesting.ui.mainScreen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.konovus.apitesting.R
import com.konovus.apitesting.TrendingItemBindingModelBuilder
import com.konovus.apitesting.data.local.entities.FavoritesRVItem
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.databinding.MainFragmentBinding
import com.konovus.apitesting.trendingItem
import com.konovus.apitesting.ui.MainActivity
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Constants.TEN_MINUTES
import com.konovus.apitesting.util.Constants.TIME_SPANS
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment), FavoritesAdapter.OnItemClickListener {

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = MainFragmentBinding.bind(view)

        binding.apply {
            bindPortfolioData()
            bindFavoritesData()
            bindTrendingData()
        }
        bindErrorHandling()
        setupListeners()
    }

    private fun MainFragmentBinding.bindFavoritesData() {
        combine(viewModel.state.map { it.favoritesList }.asFlow(),
            viewModel.store.stateFlow.map { it.chartData }) { favorites, chartData ->
            Pair(favorites, chartData)
        }.distinctUntilChanged().asLiveData().observe(viewLifecycleOwner) { pair ->
            val stocks = pair.first
            val chartData = pair.second
            Log.i(TAG, "favorites list MF: ${stocks.map { it.symbol }} | $chartData")
            if (stocks.isEmpty()) return@observe
            if (stocks.minOf { it.lastUpdatedTime } + TEN_MINUTES < System.currentTimeMillis())
                viewModel.updateFavoritesQuotes(stocks)
            if (!chartData.keys.containsAll(stocks.map {
                    it.symbol + TIME_SPANS[0].first + TIME_SPANS[0].second }))
                viewModel.updateFavoritesChartData(stocks)
            val favoritesAdapter = FavoritesAdapter(this@MainFragment)
            recyclerViewFavorites.adapter = favoritesAdapter
            recyclerViewFavorites.isVisible = true
            favoritesAdapter.submitList(stocks.map {
                FavoritesRVItem(
                    stock = it,
                    intraDayInfo = chartData[it.symbol + TIME_SPANS[0].first + TIME_SPANS[0].second]
                        ?: emptyList()
                )
            })
        }
        viewModel.state.map { Pair(it.favoritesNr, it.favoritesLoading) }.observe(viewLifecycleOwner) {
            Log.i(TAG, "bindFavoritesData: $it")
            addStocksTv.isVisible = it.first == 0
            addStocksBtn.isVisible = it.first == 0

            favoritesShimmerLayout.root.isVisible = it.first != 0 && it.second
            recyclerViewFavorites.isVisible = !it.second
            if (it.second)
                favoritesShimmerLayout.root.startShimmer()
            else favoritesShimmerLayout.root.stopShimmer()
        }
    }

    private fun MainFragmentBinding.bindTrendingData() {
        viewModel.store.stateFlow.map { it.trendingStocks }.asLiveData().observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    recyclerViewTrending.withModels {
                        it.forEach {
                            trendingItem { setupEachTrendingItem(it) }
                        }
                    }
                }
            }

        viewModel.state.map { it.trendingLoading }.observe(viewLifecycleOwner) {
            trendingShimmerLayout.root.isVisible = it
            recyclerViewTrending.isVisible = !it
            if (it)
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

    private fun MainFragmentBinding.bindPortfolioData() {
        lifecycleScope.launch {
            viewModel.store.stateFlow.map { it.portfolio }.filterNotNull().asLiveData().observe(viewLifecycleOwner) {
                Log.i(TAG, "getPortfolioData: $it , ${it.stocksToShareAmount}")

                totalBalanceTv.text = "$${it.totalBalance.toNDecimals(2)}"

                if (it.change == 0.0)
                    portfolioChangeBalanceTv.text = "$${it.change} / ${it.changeInPercentage}%"
                else if (it.change > 0) {
                    portfolioChangeBalanceTv.text = "+$${it.change} / ${it.changeInPercentage}%"
                    portfolioChangeBalanceTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    portfolioChangeBalanceTv.text = "-$${ it.change.toString().substring(1)} / ${it.changeInPercentage}%"
                    portfolioChangeBalanceTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_orange))
                }
                if (it.lastUpdatedTime + TEN_MINUTES * 1000 < System.currentTimeMillis() && it.stocksToShareAmount.isNotEmpty())
                    viewModel.onEvent(MainScreenEvents.OnRequestPortfolioUpdate(it))
            }
        }
    }

    private fun bindErrorHandling() {
        viewModel.state.map { it.error }.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        (activity as? MainActivity)?.navigateToTab(R.id.mainFragment)
        binding.addStocksBtn.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.searchFragment)
        }
    }

    override fun onFavoriteItemClick(stock: Stock) {
        val action = MainFragmentDirections.actionMainFragmentToInfoFragment(stock.name, stock.symbol)
        findNavController().navigate(action)
    }

}
