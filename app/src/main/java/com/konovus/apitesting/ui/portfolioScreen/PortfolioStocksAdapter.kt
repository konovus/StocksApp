package com.konovus.apitesting.ui.portfolioScreen


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.databinding.PortfolioStockItemBinding
import com.konovus.apitesting.databinding.StockItemBinding
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.toNDecimals

class PortfolioStocksAdapter(private val listener: OnItemClickListener, private val portfolio: Portfolio) :
    ListAdapter<Stock, PortfolioStocksAdapter.MainViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding = PortfolioStockItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class MainViewHolder(
        private val binding: PortfolioStockItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if(pos != RecyclerView.NO_POSITION){
                    getItem(pos)?.let {
                        listener.onItemClick(it, pos)
                    }
                }
            }
        }

        fun bind(stock: Stock) {

            binding.apply {
                nameTv.text = stock.name
                symbolTv.text = stock.symbol
                amountOwnedTv.text = "$${portfolio.stocksToShareAmount.getValue(stock.symbol).times(stock.price).toNDecimals(2)}"

                val initialAmount = portfolio.transactions.filter { it.symbol == stock.symbol }.filter { it.orderType == OrderType.Buy }
                    .sumOf { it.amount }.minus(portfolio.transactions.filter { it.symbol == stock.symbol }
                        .filter { it.orderType == OrderType.Sell }.sumOf { it.amount })
                val currentAmount = portfolio.stocksToShareAmount[stock.symbol]!! * stock.price
                val changeInPercent = ((currentAmount - initialAmount) / initialAmount * 100).toNDecimals(2)

                changePercentTv.text = "${changeInPercent}%"
                if (changeInPercent == 0.0)
                    changePercentTv.text = "${changeInPercent}%"
                else if (changeInPercent > 0) {
                changePercentTv.text = "+${changeInPercent}%"
                changePercentTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                } else {
                    changePercentTv.text = "-${changeInPercent}%"
                    changePercentTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.red_orange))
                }
            }
        }
    }


    interface OnItemClickListener {
        fun onItemClick(stock: Stock, position: Int)
    }

    companion object {
        private val selectedList = mutableListOf<String>()

        private val Differ = object : DiffUtil.ItemCallback<Stock>() {
            override fun areItemsTheSame(oldItem: Stock, newItem: Stock) =
                oldItem.symbol == newItem.symbol


            override fun areContentsTheSame(oldItem: Stock, newItem: Stock) =
                oldItem == newItem
        }
    }
}