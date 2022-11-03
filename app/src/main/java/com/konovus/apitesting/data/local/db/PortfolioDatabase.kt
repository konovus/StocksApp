package com.konovus.apitesting.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.konovus.apitesting.data.local.dao.PortfolioDao
import com.konovus.apitesting.data.local.entities.Converters
import com.konovus.apitesting.data.local.entities.Portfolio

@Database(entities = [Portfolio::class], version = 8)
@TypeConverters(Converters::class)
abstract class PortfolioDatabase: RoomDatabase() {

    abstract val dao: PortfolioDao
}