package com.konovus.apitesting.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Profile(
    @PrimaryKey val id: Int? = null,
    val portfolio: Portfolio = Portfolio(),
    val favorites: List<String> = emptyList(),
    val lastUpdatedTime: Long = 0L
): Parcelable {

    fun updateFavorites(symbol: String): Profile {
        val list = favorites.toMutableList()
        if (list.contains(symbol))
            list.remove(symbol)
        else list.add(symbol)
        return this.copy(favorites = list)
    }
}