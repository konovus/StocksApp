package com.konovus.apitesting.data.local.dao

import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.entities.Stock
import kotlinx.coroutines.flow.Flow

class FakeStockDao(var stocks: List<Stock>? = mutableListOf()): StockDao {
    override suspend fun insertStocks(stocks: List<Stock>) {
        TODO("Not yet implemented")
    }

    override fun getAllStocksFlow(): Flow<List<Stock>> {
        TODO("Not yet implemented")
    }

    override suspend fun clearStocks() {
        TODO("Not yet implemented")
    }

    override fun getAllFavoriteStocksFlow(): Flow<List<Stock>> {
        TODO("Not yet implemented")
    }

    override suspend fun getStockBySymbol(symbol: String): Stock? {
        TODO("Not yet implemented")
    }

    override suspend fun updateStock(stock: Stock) {
        TODO("Not yet implemented")
    }

    override suspend fun updateStocks(stocks: List<Stock>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertStock(stock: Stock) {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalFavStocks(): Int {
        TODO("Not yet implemented")
    }
}