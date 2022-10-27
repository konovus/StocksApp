package com.konovus.apitesting.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.konovus.apitesting.data.local.dao.CompanyDao
import com.konovus.apitesting.data.local.entities.CompanyInfo

@Database(entities = [CompanyInfo::class], version = 2)
abstract class CompaniesDatabase: RoomDatabase() {

    abstract val dao: CompanyDao
}