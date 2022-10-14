package com.konovus.apitesting.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.konovus.apitesting.data.local.entities.Portfolio
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertPortfolio(portfolio: Portfolio)

    @Update(onConflict = REPLACE)
    suspend fun updatePortfolio(portfolio: Portfolio)

    @Query("select * from portfolios_table order by id asc")
    fun getAllPortfolios(): Flow<List<Portfolio>>

    @Query("select * from portfolios_table where :id = id")
    suspend fun getPortfolioById(id: Int = 1): Portfolio?

}