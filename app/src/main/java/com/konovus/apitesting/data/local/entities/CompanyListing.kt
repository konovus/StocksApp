package com.konovus.apitesting.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CompanyListing(
    val name: String,
    val symbol: String,
    val exchange: String,
    val description: String,
    val country: String,
    val industry: String,
    @PrimaryKey val id: Int? = null
) {

    fun toCompanyInfo() =
        CompanyInfo(
            name = name,
            symbol = symbol,
            exchange = exchange,
        )
}