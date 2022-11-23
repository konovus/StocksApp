package com.konovus.apitesting.data.local.models

import com.konovus.apitesting.data.local.entities.ChartData

data class FavoritesUiModel(
    val quote: Quote,
    val chartData: List<ChartData>
)