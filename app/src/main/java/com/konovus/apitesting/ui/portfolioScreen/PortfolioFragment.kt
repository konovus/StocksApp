package com.konovus.apitesting.ui.portfolioScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.konovus.apitesting.PortfolioStockItemBindingModelBuilder
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.databinding.PortfolioFragmentBinding
import com.konovus.apitesting.portfolioStockItem
import com.konovus.apitesting.ui.MainActivity
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PortfolioFragment: Fragment(R.layout.portfolio_fragment) {

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

    private fun initLayout() = binding.apply {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.portfolio == null) return@observe

            portfolio = state.portfolio
            stocks = state.stocks
            recyclerView.withModels {
                state.stocks.forEach { stock ->
                    portfolioStockItem {
                        setupEachPortfolioStockItem(stock, state.portfolio)
                    }
                }
            }
        }
    }

    private fun PortfolioStockItemBindingModelBuilder.setupEachPortfolioStockItem(stock: Stock, portfolio: Portfolio) {
        id(stock.symbol)
        stock(stock)
        val amountOwned = portfolio.stocksToShareAmount.getValue(stock.symbol)
            .times(stock.price).toNDecimals(2)
        amountOwned(amountOwned.toString())
        val initialAmount = portfolio.transactions.filter { it.symbol == stock.symbol }.filter { it.orderType == OrderType.Buy }
            .sumOf { it.amount }.minus(portfolio.transactions.filter { it.symbol == stock.symbol }
                .filter { it.orderType == OrderType.Sell }.sumOf { it.amount })
        val currentAmount = portfolio.stocksToShareAmount[stock.symbol]!! * stock.price
        val changeInPercent = ((currentAmount - initialAmount) / initialAmount * 100).toNDecimals(2)
        changePercentValue(changeInPercent)
        changePercent(changeInPercent.toString())
        onClick{ _ ->
            val action = PortfolioFragmentDirections.actionPortfolioFragmentToInfoFragment(stock.name, stock.symbol)
            findNavController().navigate(action)
        }
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
}