package com.konovus.apitesting.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.konovus.apitesting.data.local.entities.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(profile: Profile)

    @Update(onConflict = REPLACE)
    suspend fun update(profile: Profile)

    @Query("select * from profile")
    fun getProfileFlow(): Flow<Profile?>

    @Query("select * from profile where :id = id")
    suspend fun getProfileById(id: Int = 1): Profile?
}