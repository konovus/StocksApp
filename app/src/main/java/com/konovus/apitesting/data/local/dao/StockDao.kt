package com.konovus.apitesting.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.konovus.apitesting.data.local.entities.Stock
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertStocks(stocks: List<Stock>)

    @Query("select * from stocks_table order by changePercent desc")
    fun getAllStocksFlow(): Flow<List<Stock>>

    @Query("select * from stocks_table ")
    suspend fun getAllStocks(): List<Stock>

    @Query("delete from stocks_table")
    suspend fun clearStocks()


    @Query("Select * from stocks_table WHERE isFavorite = 1")
    fun getAllFavoriteStocksFlow(): Flow<List<Stock>>

    @Query("Select * from stocks_table WHERE isFavorite = 1")
    fun getAllFavoriteStocks(): LiveData<List<Stock>>

    @Query("select * from stocks_table where :symbol = symbol")
    suspend fun getStockBySymbol(symbol: String): Stock?

    @Update(onConflict = REPLACE)
    suspend fun updateStock(stock: Stock)

    @Update(onConflict = REPLACE)
    suspend fun updateStocks(stocks: List<Stock>)

    @Insert(onConflict = REPLACE)
    suspend fun insertStock(stock: Stock)

    @Query("SELECT count(*) FROM stocks_table where isFavorite = 1")
    suspend fun getTotalFavStocks(): Int
}