package com.konovus.apitesting.data.local.entities

data class UiModelFavoriteItem(
    val stock: Stock,
    val intraDayInfos: List<ChartData>
)
