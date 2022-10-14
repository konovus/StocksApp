package com.konovus.apitesting.ui.portfolioScreen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.databinding.PortfolioFragmentBinding
import com.konovus.apitesting.ui.MainActivity
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Constants.TEN_MINUTES
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PortfolioFragment: Fragment(R.layout.portfolio_fragment),
    PortfolioStocksAdapter.OnItemClickListener {

    private var _binding: PortfolioFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PortfolioViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = PortfolioFragmentBinding.bind(view)

        initLayout()
        setupListeners()
        bindErrorHandling()
    }

    private fun initLayout() {
        lifecycleScope.launch {
            viewModel.state.observe(viewLifecycleOwner) { state ->
                if (state.portfolio == null) return@observe

                binding.bindPortfolioData(state.portfolio)
                binding.noStocksOwnedTv.isVisible = state.stocks.isEmpty()
                binding.addStocksBtn.isVisible = state.stocks.isEmpty()
                val adapter = PortfolioStocksAdapter(this@PortfolioFragment, state.portfolio)
                binding.recyclerView.adapter = adapter
                adapter.submitList(state.stocks)
            }
        }
    }

    private fun PortfolioFragmentBinding.bindPortfolioData(portfolio: Portfolio) {
        Log.i(TAG, "getPortfolioData: P $portfolio ")

        totalBalanceTv.text = "$${portfolio.totalBalance.toNDecimals(2)}"

        if (portfolio.change == 0.0)
            portfolioChangeBalanceTv.text = "$${portfolio.change} / ${portfolio.changeInPercentage}%"
        else if (portfolio.change > 0) {
            portfolioChangeBalanceTv.text = "+$${portfolio.change} / ${portfolio.changeInPercentage}%"
            portfolioChangeBalanceTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        } else {
            portfolioChangeBalanceTv.text = "-$${ portfolio.change.toString().substring(1)} / ${portfolio.changeInPercentage}%"
            portfolioChangeBalanceTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_orange))
        }
        stocksNr.text = "${portfolio.stocksToShareAmount.keys.size}"
        if (portfolio.lastUpdatedTime + TEN_MINUTES < System.currentTimeMillis() && portfolio.stocksToShareAmount.isNotEmpty())
            viewModel.requestPortfolioUpdate(portfolio)
    }

    private fun setupListeners() {
        (activity as? MainActivity)?.navigateToTab(R.id.portfolioFragment)
        binding.addStocksBtn.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.searchFragment)
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

    override fun onItemClick(stock: Stock, position: Int) {
        val action = PortfolioFragmentDirections.actionPortfolioFragmentToInfoFragment(stock.name, stock.symbol)
        findNavController().navigate(action)
    }
}