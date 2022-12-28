package com.konovus.apitesting.ui.portfolioScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.konovus.apitesting.PortfolioStockItemBindingModelBuilder
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.databinding.PortfolioFragmentBinding
import com.konovus.apitesting.portfolioStockItem
import com.konovus.apitesting.ui.MainActivity
import com.konovus.apitesting.util.NetworkConnectionObserver
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PortfolioFragment: Fragment(R.layout.portfolio_fragment) {

    private var _binding: PortfolioFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PortfolioViewModel by viewModels()
    @Inject
    lateinit var networkConnectionObserver: NetworkConnectionObserver


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = PortfolioFragmentBinding.bind(view)

        bindData()
        setupListeners()
    }

    private fun bindData() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.portfolio == null) return@observe

            binding.portfolio = state.portfolio
            binding.stocks = state.quotes
            binding.recyclerView.withModels {
                state.quotes.forEach { quote ->
                    portfolioStockItem {
                        setupEachPortfolioStockItem(quote, state.portfolio)
                    }
                }
            }
        }
    }

    private fun PortfolioStockItemBindingModelBuilder.setupEachPortfolioStockItem(quote: Quote, portfolio: Portfolio) {
        id(quote.symbol)
        quote(quote)
        val amountOwned = portfolio.stocksToShareAmount.getValue(quote.symbol)
            .times(quote.price.toDouble()).toNDecimals(2)
        amountOwned(amountOwned.toString())
        val initialAmount = portfolio.transactions.filter { it.symbol == quote.symbol }.filter { it.orderType == OrderType.Buy }
            .sumOf { it.amount }.minus(portfolio.transactions.filter { it.symbol == quote.symbol }
                .filter { it.orderType == OrderType.Sell }.sumOf { it.amount })
        val currentAmount = (portfolio.stocksToShareAmount[quote.symbol]!! * quote.price.toDouble()).toNDecimals(2)
        val changeInPercent = ((currentAmount - initialAmount) / initialAmount * 100).toNDecimals(2)
        changePercentValue(changeInPercent)
        changePercent(changeInPercent.toString())
        onClick{ _ ->
            val action = PortfolioFragmentDirections.actionPortfolioFragmentToInfoFragment(quote.name, quote.symbol)
            findNavController().navigate(action)
        }
    }

    private fun setupListeners() {
        (activity as? MainActivity)?.navigateToTab(R.id.portfolioFragment)
        binding.addStocksBtn.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.searchFragment)
        }
    }

}