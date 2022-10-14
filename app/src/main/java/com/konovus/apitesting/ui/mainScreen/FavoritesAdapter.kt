package com.konovus.apitesting.ui.mainScreen


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.FavoritesRVItem
import com.konovus.apitesting.data.local.entities.IntraDayInfo
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.databinding.FavoritesStockItemBinding
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.toNDecimals

class FavoritesAdapter(private val listener: OnItemClickListener) :
    ListAdapter<FavoritesRVItem, FavoritesAdapter.MainViewHolder>(Differ) {

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
                        listener.onFavoriteItemClick(it.stock)
                    }
                }
            }
        }

        fun bind(item: FavoritesRVItem) {

            binding.apply {
                name.text = item.stock.name
                symbol.text = item.stock.symbol
                price.text = item.stock.price.toNDecimals(2).toString()

                setupChart(item.intraDayInfo, chart, itemView.context)

            }
        }
    }

    private fun setupChart(stockInfos: List<IntraDayInfo>, chart: LineChart, context: Context) {
        chart.onTouchListener = null

        val entries = mutableListOf<Entry>()
        stockInfos.forEachIndexed { i, it ->
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
        fun onFavoriteItemClick(stock: Stock)
    }

    companion object {
        private val Differ = object : DiffUtil.ItemCallback<FavoritesRVItem>() {
            override fun areItemsTheSame(oldItem: FavoritesRVItem, newItem: FavoritesRVItem) =
                oldItem.stock.symbol == newItem.stock.symbol

            override fun areContentsTheSame(oldItem: FavoritesRVItem, newItem: FavoritesRVItem) =
                oldItem == newItem
        }
    }
}