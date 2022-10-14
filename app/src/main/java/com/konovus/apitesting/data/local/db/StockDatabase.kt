package com.konovus.apitesting.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.data.local.entities.CompanyListing
import com.konovus.apitesting.data.local.entities.Converters
import com.konovus.apitesting.data.local.entities.Stock

@Database(entities = [Stock::class], version = 10)
abstract class StockDatabase: RoomDatabase() {
    abstract val dao: StockDao
}