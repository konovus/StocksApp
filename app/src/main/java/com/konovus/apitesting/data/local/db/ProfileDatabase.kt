package com.konovus.apitesting.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.konovus.apitesting.data.local.dao.ProfileDao
import com.konovus.apitesting.data.local.entities.Converters
import com.konovus.apitesting.data.local.entities.Profile

@Database(entities = [Profile::class], version = 1)
@TypeConverters(Converters::class)
abstract class ProfileDatabase: RoomDatabase() {

    abstract val dao: ProfileDao
}