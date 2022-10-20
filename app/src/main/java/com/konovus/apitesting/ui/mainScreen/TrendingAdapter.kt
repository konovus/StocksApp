package com.konovus.apitesting.ui.mainScreen


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.databinding.TrendingItemBinding

class TrendingAdapter(
    private val listener: OnItemClickListener
) :
    ListAdapter<Stock, TrendingAdapter.MainViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding = TrendingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class MainViewHolder(
        private val binding: TrendingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if(pos != RecyclerView.NO_POSITION){
                    getItem(pos)?.let {
                        listener.onTrendingItemClick(it)
                    }
                }
            }
        }

        fun bind(stock: Stock) {

            binding.apply {
                name.text = stock.name
                symbol.text = stock.symbol
                price.text = "${stock.price}$"
                change.text = "${stock.changePercent}%"

                if (stock.change > 0)
                    change.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                else change.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red_orange))



//                    setupChart(binding, itemView.context, intraDayInfo)
//                    stockInfoData[stock.symbol]?.let {
//                        setupChart(binding, itemView.context, it)
//                    }

            }
        }
    }



    interface OnItemClickListener {
        fun onTrendingItemClick(stock: Stock)
    }
    companion object {

        private val Differ = object : DiffUtil.ItemCallback<Stock>() {
            override fun areItemsTheSame(oldItem: Stock, newItem: Stock) =
                oldItem.symbol == newItem.symbol

            override fun areContentsTheSame(oldItem: Stock, newItem: Stock) =
                oldItem == newItem
        }
    }
}

//private fun setupChart(binding: HorizontalCompanyItemBinding, context: Context, stockInfos: List<IntraDayInfo>) {
//    binding.apply {
//
//        val entries = mutableListOf<Entry>()
//        stockInfos.forEach {
//            entries.add(Entry(it.toDateTime().hour.toFloat(), it.close.toFloat()))
//        }
//
//        chart.setBackgroundColor(R.attr.fillColor)
//        chart.setGridBackgroundColor(R.attr.fillColor)
//        chart.setDrawGridBackground(false)
//
//        chart.setDrawBorders(false)
//        chart.description.isEnabled = false
//        chart.setPinchZoom(false)
//        chart.setViewPortOffsets(0f, 0f, 0f, 0f)
//
//        val l: Legend = chart.legend
//        l.isEnabled = false
//
//        val xAxis: XAxis = chart.xAxis
//        xAxis.isEnabled = false
//        xAxis.valueFormatter = object : ValueFormatter() {
//            override fun getFormattedValue(value: Float): String {
//                return value.toString().split(".")[0]
//            }
//        }
//        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.setAvoidFirstLastClipping(true)
//        xAxis.setDrawAxisLine(false)
//        xAxis.setDrawGridLines(false)
//        xAxis.setLabelCount(if (entries.size < 8) entries.size else 8, true)
//        xAxis.textColor = ContextCompat.getColor(context, com.konovus.apitesting.R.color.dark_gray)
//        xAxis.textSize = 14f
//        xAxis.yOffset = 5f
//        val axisRight: YAxis = chart.axisRight
//        axisRight.setDrawGridLines(false)
//        axisRight.isEnabled = false
//        val axisLeft: YAxis = chart.axisLeft
//        axisLeft.isEnabled = false
//        axisLeft.setDrawAxisLine(false)
//        axisLeft.textSize = 13f
//        axisLeft.xOffset = 10f
//        axisLeft.textColor = ContextCompat.getColor(context, com.konovus.apitesting.R.color.dark_gray)
//        axisLeft.setLabelCount(4, true)
//        axisLeft.setDrawZeroLine(false)
//        axisLeft.setDrawGridLines(false)
//        axisLeft.setDrawAxisLine(false)
//
//
//        chart.extraBottomOffset = 10f
//
//        val lineDataSet = LineDataSet(entries, "Default entries")
//        val lineData = LineData(lineDataSet)
//        chart.data = lineData
//        lineDataSet.setDrawFilled(true)
//        val fillGradient =
//            ContextCompat.getDrawable(context, com.konovus.apitesting.R.drawable.chart_gradient)
//        lineDataSet.fillDrawable = fillGradient
//        lineDataSet.setDrawCircles(false)
//        lineDataSet.setDrawValues(false)
//        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
//        chart.invalidate()
//    }
//}
