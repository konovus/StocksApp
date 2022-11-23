package com.konovus.apitesting.data.local.models

data class Quote(
    val symbol: String = "",
    val name: String = "",
    val price: String = "",
    val lastTimeUpdated: Long = 0L
)
