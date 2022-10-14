package com.konovus.apitesting.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.konovus.apitesting.data.local.dao.CompanyDao
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.data.local.entities.CompanyListing
import com.konovus.apitesting.data.local.entities.Converters
import com.konovus.apitesting.data.local.entities.Stock

@Database(entities = [CompanyInfo::class], version = 2)
abstract class CompaniesDatabase: RoomDatabase() {

    abstract val dao: CompanyDao
}