package com.konovus.apitesting.ui.mainScreen


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.databinding.PortfolioItemBinding
import com.konovus.apitesting.util.toNDecimals

class PortfoliosAdapter(
    private val listener: OnItemClickListener
    ) : ListAdapter<Portfolio, PortfoliosAdapter.MainViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding =
            PortfolioItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class MainViewHolder(private val binding: PortfolioItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if(pos != RecyclerView.NO_POSITION){
                    getItem(pos)?.let {
                        listener.onPortfolioItemClick(it)
                    }
                }
            }
        }

        fun bind(portfolio: Portfolio) {

            binding.apply {
                name.text = portfolio.name
                totalStocks.text = "Total stocks: ${portfolio.stocksToShareAmount.size}"
                totalValue.text = portfolio.totalBalance.toNDecimals(2).toString()
            }
        }

    }

    interface OnItemClickListener {
        fun onPortfolioItemClick(portfolio: Portfolio)
    }

    companion object {

        private val Differ = object : DiffUtil.ItemCallback<Portfolio>() {
            override fun areItemsTheSame(oldItem: Portfolio, newItem: Portfolio) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Portfolio, newItem: Portfolio) =
                oldItem == newItem
        }
    }
}