package com.konovus.apitesting.data.remote.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChartsDataResponse(
    val chartPreviousClose: Double,
    val close: List<Double>,
    val dataGranularity: Int,
    val end: Int,
    val previousClose: Double,
    val start: Int,
    val symbol: String,
    val timestamp: List<Double>
): Parcelable {
}