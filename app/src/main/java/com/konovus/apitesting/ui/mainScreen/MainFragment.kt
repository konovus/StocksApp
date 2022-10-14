package com.konovus.apitesting.ui.mainScreen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.FavoritesRVItem
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.databinding.MainFragmentBinding
import com.konovus.apitesting.ui.MainActivity
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Constants.TEN_MINUTES
import com.konovus.apitesting.util.Constants.TIME_SPANS
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment),
    TrendingAdapter.OnItemClickListener, FavoritesAdapter.OnItemClickListener {

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
        viewModel.state.map { it.favoritesList }.observe(viewLifecycleOwner){ stocks ->
            addStocksTv.isVisible = stocks.isEmpty()
            addStocksBtn.isVisible = stocks.isEmpty()
            if (stocks.isEmpty()) return@observe
            if (stocks.minOf { it.priceLastUpdated } + TEN_MINUTES < System.currentTimeMillis())
                viewModel.updatePricesForFavorites(stocks)
            Log.i(TAG, "favorites list MF: ${stocks.size}")
            val favoritesAdapter = FavoritesAdapter(this@MainFragment)
            recyclerViewFavorites.adapter = favoritesAdapter
            recyclerViewFavorites.isVisible = true
            favoritesAdapter.submitList(stocks.map {
                FavoritesRVItem(
                    stock = it,
                    intraDayInfo = viewModel.store.stateFlow.value
                        .chartData[it.symbol + TIME_SPANS[0].first + TIME_SPANS[0].second] ?: emptyList()
                )
            })
        }

        viewModel.state.map { it.favoritesLoading }.observe(viewLifecycleOwner) {
            favoritesShimmerLayout.root.isVisible = it
            recyclerViewFavorites.isVisible = !it
            if (it)
                favoritesShimmerLayout.root.startShimmer()
            else favoritesShimmerLayout.root.stopShimmer()
        }
    }

    private fun MainFragmentBinding.bindTrendingData() {
        viewModel.store.stateFlow.map { it.trendingStocks }.asLiveData().observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    val trendingAdapter = TrendingAdapter(this@MainFragment)
                    recyclerViewTrending.adapter = trendingAdapter
                    trendingAdapter.submitList(it)
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
                if (it.lastUpdatedTime + TEN_MINUTES < System.currentTimeMillis() && it.stocksToShareAmount.isNotEmpty())
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

    override fun onTrendingItemClick(stock: Stock) {
        val action = MainFragmentDirections.actionMainFragmentToInfoFragment(stock.name, stock.symbol)
        findNavController().navigate(action)
    }

    override fun onFavoriteItemClick(stock: Stock) {
        val action = MainFragmentDirections.actionMainFragmentToInfoFragment(stock.name, stock.symbol)
        findNavController().navigate(action)
    }

}
