package com.konovus.apitesting.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.entities.Converters
import com.konovus.apitesting.data.local.entities.Stock

@Database(entities = [Stock::class], version = 13)
@TypeConverters(Converters::class)
abstract class StockDatabase: RoomDatabase() {
    abstract val dao: StockDao
}