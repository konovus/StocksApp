package com.konovus.apitesting.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.konovus.apitesting.data.local.entities.CompanyInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertCompanyInfo(
        companyInfo: List<CompanyInfo>
    )

    @Query("select * from companyinfo where :symbol = symbol")
    suspend fun getCompanyInfoBySymbol(
        symbol: String
    ): CompanyInfo

    @Update(onConflict = REPLACE)
    suspend fun updateCompanyInfo(companyInfo: CompanyInfo)

    @Query("delete from companyinfo")
    suspend fun clearCompanyInfos()

    @Query("select * from companyinfo where " +
            "lower(name) like '%' || lower(:query) || '%' or " +
            "upper(:query) == symbol")
    suspend fun searchCompanyInfo(
        query: String
    ): List<CompanyInfo>

    @Query("select count(*) from companyinfo")
    suspend fun getTotalRows(): Int

    @Query("select * from companyinfo where " +
            "lower(name) like '%' || lower(:query) || '%' or " +
            "upper(:query) == symbol ORDER BY CASE " +
            "WHEN upper(:query) == symbol THEN 1 " +
            "ELSE 2 END")
    fun searchCompanyInfoPaged(
        query: String = ""
    ): PagingSource<Int, CompanyInfo>

    @Query("SELECT EXISTS (SELECT 1 FROM companyinfo WHERE symbol = :symbol)")
    suspend fun exists(symbol: String): Boolean

    @Query("Select * from companyinfo WHERE isFavorite = 1")
    fun getAllFavoriteStocks(): Flow<List<CompanyInfo>>

}