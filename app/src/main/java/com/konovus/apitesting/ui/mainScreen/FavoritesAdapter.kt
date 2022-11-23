package com.konovus.apitesting.ui.mainScreen


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.models.FavoritesUiModel
import com.konovus.apitesting.data.local.models.Quote
import com.konovus.apitesting.databinding.FavoritesStockItemBinding
import com.konovus.apitesting.util.toNDecimals

class FavoritesAdapter(private val listener: OnItemClickListener) :
    ListAdapter<FavoritesUiModel, FavoritesAdapter.MainViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding =
            FavoritesStockItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class MainViewHolder(
        private val binding: FavoritesStockItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    getItem(pos)?.let {
                        listener.onFavoriteItemClick(it.quote)
                    }
                }
            }
        }

        fun bind(item: FavoritesUiModel) {

            binding.apply {
                name.text = item.quote.name
                symbol.text = item.quote.symbol
                price.text = item.quote.price.toNDecimals(2).toString()

                setupChart(item.chartData, chart, itemView.context)

            }
        }
    }

    override fun submitList(list: List<FavoritesUiModel>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    private fun setupChart(chartData: List<ChartData>, chart: LineChart, context: Context) {
        chart.onTouchListener = null

        val entries = mutableListOf<Entry>()
        chartData.forEachIndexed { i, it ->
            entries.add(Entry(i.toFloat(), it.close.toFloat()))
        }
        chart.setBackgroundColor(android.R.attr.fillColor)
        chart.setGridBackgroundColor(android.R.attr.fillColor)
        chart.setDrawGridBackground(false)

        chart.setDrawBorders(false)
        chart.description.isEnabled = false
        chart.setPinchZoom(false)

        val l: Legend = chart.legend
        l.isEnabled = false

        val xAxis: XAxis = chart.xAxis
        xAxis.isEnabled = false
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        val axisRight: YAxis = chart.axisRight
        axisRight.setDrawGridLines(false)
        axisRight.isEnabled = false
        val axisLeft: YAxis = chart.axisLeft
        axisLeft.isEnabled = false
        axisLeft.setDrawZeroLine(false)
        axisLeft.setDrawGridLines(false)
        axisLeft.setDrawAxisLine(false)


        chart.extraBottomOffset = 10f

        val lineDataSet = LineDataSet(entries, "Default entries")
        val lineData = LineData(lineDataSet)
        chart.data = lineData
        lineDataSet.setDrawFilled(true)
        val fillGradient =
            ContextCompat.getDrawable(context, R.drawable.chart_gradient_fav_item)
        lineDataSet.fillDrawable = fillGradient
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        chart.invalidate()
    }


    interface OnItemClickListener {
        fun onFavoriteItemClick(quote: Quote)
    }

    companion object {
        private val Differ = object : DiffUtil.ItemCallback<FavoritesUiModel>() {
            override fun areItemsTheSame(oldItem: FavoritesUiModel, newItem: FavoritesUiModel) =
                oldItem.quote.symbol == newItem.quote.symbol

            override fun areContentsTheSame(oldItem: FavoritesUiModel, newItem: FavoritesUiModel) =
                oldItem == newItem
        }
    }
}