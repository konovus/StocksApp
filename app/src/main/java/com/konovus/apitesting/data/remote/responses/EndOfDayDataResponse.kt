package com.konovus.apitesting.data.remote.responses

import com.konovus.apitesting.util.withSuffix

data class EndOfDayDataResponse(
    val close: Double,
    val from: String,
    val high: Double,
    val low: Double,
    val open: Double,
    val symbol: String,
    val volume: Int
) {
    fun volumeWithSuffix() = volume.toString().withSuffix()
}