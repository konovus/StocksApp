package com.konovus.apitesting.ui.infoScreen

import android.R.attr.fillColor
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.entities.Transaction
import com.konovus.apitesting.databinding.BottomSheetBinding
import com.konovus.apitesting.databinding.InfoFragmentBinding
import com.konovus.apitesting.transactionsItem
import com.konovus.apitesting.util.MpMarker
import com.konovus.apitesting.util.OnTabSelected
import com.konovus.apitesting.util.toNDecimals
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull


@AndroidEntryPoint
class InfoFragment : Fragment(R.layout.info_fragment) {

    private var _binding: InfoFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InfoScreenViewModel by viewModels()
    private val args: InfoFragmentArgs by navArgs()
    private lateinit var stock: Stock

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = InfoFragmentBinding.bind(view)

        bindUiStateToLayout()
        bindTransactionsData()
        setupListeners()
        bindErrorHandling()

        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).isVisible = false
    }

    private fun bindUiStateToLayout() {
        viewModel.state.distinctUntilChanged().observe(viewLifecycleOwner) { state ->
            binding.state = state
            state.stock?.let { stock = it }
            setupChart(state.chartData)
        }
    }

    private fun bindTransactionsData() = binding.apply {
        combine(viewModel.state.map { it.portfolio?.transactions }.asFlow().filterNotNull(),
            viewModel.state.map { it.tabNr }.asFlow(), ::Pair).distinctUntilChanged()
            .asLiveData().observe(viewLifecycleOwner) { pair ->
            val transactions = pair.first.filter { it.symbol == args.symbol }
            noTransactions = transactions.isEmpty() == true
            if (transactions.isEmpty() || pair.second != 2) return@observe

            transactionsTab.epoxyRecyclerView.withModels {
                transactions.sortedByDescending { it.dateTime }
                    .forEach { transaction ->
                    transactionsItem {
                        id(transaction.dateTime)
                        transactionData(transaction)
                        isBuy(transaction.orderType == OrderType.Buy)
                    }
                }
            }
        }
    }

    private fun showBottomSheet() {
        if (viewModel.state.value?.portfolio == null) return

        val bottomSheet = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bottomSheetBinding.root)
        bottomSheet.show()
        bottomSheetBinding.apply {
            symbolTv.text = stock.symbol
            priceTv.text = "$${stock.price}"

            var orderType = OrderType.Buy
            buySellSwitch.buyBtn.setOnClickListener {
                orderType = OrderType.Buy
                sumInput.text = sumInput.text
                sumInput.clearFocus()
            }
            buySellSwitch.sellBtn.setOnClickListener {
                orderType = OrderType.Sell
                sumInput.text = sumInput.text
                sumInput.clearFocus()
            }
            sumInput.doOnTextChanged { text, _, _, _ ->
                bottomSheetBinding.validateAmountInput(text, orderType)
            }
            confirmBtn.setOnClickListener {
                if ( !bottomSheetBinding.confirmTransaction(orderType)) return@setOnClickListener
                bottomSheet.hide()
                Toast.makeText(requireContext(), "$orderType order confirmed", Toast.LENGTH_LONG)
                        .show()
            }
        }
    }

    private fun BottomSheetBinding.confirmTransaction(orderType: OrderType): Boolean {
        if (sumInput.text.isNullOrEmpty()) sumInputWrap.error = "Amount cannot be empty"
        if (!sumInputWrap.error.isNullOrEmpty()) return false

        viewModel.onEvent(
            InfoScreenEvent.OnMarketOrderEvent(
                Transaction(
                    price = stock.price,
                    symbol = stock.symbol,
                    amount = sumInput.text.toString().toDouble().toNDecimals(2),
                    dateTime = System.currentTimeMillis(),
                    orderType = orderType
                )
            )
        )
        return true
    }

    private fun BottomSheetBinding.validateAmountInput(text: CharSequence?, orderType: OrderType) {
        if (text.isNullOrEmpty()) return

        if (orderType == OrderType.Sell) {
            val portfolio = viewModel.state.value?.portfolio!!
            if (portfolio.stocksToShareAmount.isEmpty()) {
                sumInputWrap.error = "No shares to sell"
                return
            }
            val amountToSell = text.toString().toDouble()
            val amountOwned = portfolio.stocksToShareAmount.getValue(args.symbol).times(
                stock.price).toNDecimals(2)
            if (amountToSell > amountOwned)
                sumInputWrap.error = "Max to sell is $amountOwned"
            else sumInputWrap.error = null
        } else {
            if (text.toString().toDouble() !in 1.0..10000.0)
                sumInputWrap.error = "Amount must be between 1 and 10000!"
            else sumInputWrap.error = null
        }
    }

    private fun setupChart(chartData: List<ChartData>?) = binding.apply {
        if (chartData == null) return@apply
        val entries = mutableListOf<Entry>()
        chartData.forEachIndexed { i, it ->
            entries.add(Entry(i.toFloat(), it.close.toFloat()))
        }

        val axisLeft: YAxis = chart.axisLeft
        axisLeft.textSize = 13f
        axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.gray)
        axisLeft.setLabelCount(4, true)
        axisLeft.setDrawGridLines(false)
        axisLeft.setDrawAxisLine(false)

        val lineDataSet = LineDataSet(entries, "Default entries")
        val lineData = LineData(lineDataSet)
        lineDataSet.setDrawFilled(true)
        val fillGradient = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient)
        lineDataSet.fillDrawable = fillGradient
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.setDrawHorizontalHighlightIndicator(false)
        val marker = MpMarker( chart, binding.mpMarkerTv, binding.scrollView)

        chart.marker = marker
        chart.axisRight.isEnabled = false
        chart.xAxis.isEnabled = false
        chart.legend.isEnabled = false
        chart.extraBottomOffset = 10f
        chart.data = lineData
        chart.setBackgroundColor(fillColor)
        chart.setDrawBorders(false)
        chart.description.isEnabled = false
        chart.setPinchZoom(false)
        chart.invalidate()
    }

    private fun setupListeners() = binding.apply {
        backArrow.setOnClickListener { requireActivity().onBackPressed() }
        follow.setOnClickListener { viewModel.onEvent(InfoScreenEvent.OnFavorite(stock))}
        tradeBtn.setOnClickListener { showBottomSheet() }
        timespansWrap.children.toList().forEachIndexed { index, view ->
            view.setOnClickListener {
                viewModel.onEvent(InfoScreenEvent.OnChangeChartTimeSpan(index))
                selected = index
            }
        }
        tabLayout.tabLayout.OnTabSelected {
            viewModel.onEvent(InfoScreenEvent.OnChangeTab(it))
        }
    }

    private fun bindErrorHandling() {
        viewModel.event.asLiveData().observe(viewLifecycleOwner) { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).isVisible = true
        _binding = null
    }
}

