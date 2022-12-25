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
import com.konovus.apitesting.data.local.entities.*
import com.konovus.apitesting.data.validator.NullOrEmptyValidator
import com.konovus.apitesting.data.validator.TransactionValidator
import com.konovus.apitesting.data.validator.ValidateResult
import com.konovus.apitesting.databinding.BottomSheetBinding
import com.konovus.apitesting.databinding.InfoFragmentBinding
import com.konovus.apitesting.transactionsItem
import com.konovus.apitesting.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@AndroidEntryPoint
class InfoFragment : Fragment(R.layout.info_fragment) {

    private var _binding: InfoFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InfoScreenViewModel by viewModels()
    private val args: InfoFragmentArgs by navArgs()
    private lateinit var stock: Stock
    private lateinit var profile: Profile
    @Inject
    lateinit var networkConnectionObserver: NetworkConnectionObserver

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = InfoFragmentBinding.bind(view)

        observeNetworkConnectivity()
        observeProfile()
        bindUiStateToLayout()
        bindTransactionsData()
        setupListeners()
        bindErrorHandling()

        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).isVisible = false
    }

    private fun observeNetworkConnectivity() {
        networkConnectionObserver.connection.observe(viewLifecycleOwner) {
            if (it == NetworkStatus.Available || it == NetworkStatus.BackOnline)
                viewModel.initSetup()
        }
    }

    private fun observeProfile() {
        viewModel.profile.distinctUntilChanged().asLiveData()
            .observe(viewLifecycleOwner) { profile = it }
    }

    private fun bindUiStateToLayout() {
        viewModel.state.distinctUntilChanged().observe(viewLifecycleOwner) { state ->
            binding.state = state
            state.stock?.let { stock = it }
            setupChart(state.chartData)
        }
    }

    private fun bindTransactionsData() = binding.apply {
        combine(viewModel.profile.map { it.portfolio.transactions },
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
                checkValidation(TransactionValidator(text.toString(), orderType, profile.portfolio, stock, requireContext()).validate())
            }
            confirmBtn.setOnClickListener {
                checkValidation(NullOrEmptyValidator(sumInput.text.toString(), requireContext()).validate())
                if (sumInputWrap.error != null) return@setOnClickListener
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
                bottomSheet.hide()
                Toast.makeText(requireContext(), "$orderType order confirmed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun BottomSheetBinding.checkValidation(validationResult: ValidateResult) {
        if (validationResult.isSuccess)
            sumInputWrap.error = null
        else sumInputWrap.error = validationResult.message
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

