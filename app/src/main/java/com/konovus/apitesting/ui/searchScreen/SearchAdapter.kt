package com.konovus.apitesting.ui.searchScreen


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.databinding.SearchStockItemBinding

class SearchAdapter(private val listener: OnItemClickListener) :
    PagingDataAdapter<CompanyInfo, SearchAdapter.MainViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding = SearchStockItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
//    fun clearSelectedItems() = selectedList.clear()

    inner class MainViewHolder(
        private val binding: SearchStockItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if(pos != RecyclerView.NO_POSITION){
                    getItem(pos)?.let {
                        listener.onItemClick(it, pos)
//                        if (selectedList.contains(it.symbol))
//                            selectedList.remove(it.symbol)
//                        else selectedList.add(it.symbol)
//                        notifyItemChanged(pos)
                    }
                }
            }
//            binding.favoriteIv.setOnClickListener {
//                val pos = bindingAdapterPosition
//                if (pos != RecyclerView.NO_POSITION) {
//                    getItem(pos)?.let {
//                        listener.onFavoritesClick(it.symbol)
//                    }
//                }
//            }
        }

        fun bind(company: CompanyInfo) {

            binding.apply {
                name.text = company.name
                symbol.text = company.symbol
                exchange.text = company.exchange

//                if (company.isFavorite)
//                    favoriteIv.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_baseline_star_24))
//                else favoriteIv.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_baseline_star_outline_24))


//                if (selectedList.contains(company.symbol))
//                    itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.paragraph_bg_shape)
//                else itemView.background = null


            }
        }
    }


    interface OnItemClickListener {
        fun onItemClick(company: CompanyInfo, position: Int)
    }

    companion object {

        private val Differ = object : DiffUtil.ItemCallback<CompanyInfo>() {
            override fun areItemsTheSame(oldItem: CompanyInfo, newItem: CompanyInfo) =
                oldItem.symbol == newItem.symbol


            override fun areContentsTheSame(oldItem: CompanyInfo, newItem: CompanyInfo) =
                oldItem == newItem
        }
    }
}