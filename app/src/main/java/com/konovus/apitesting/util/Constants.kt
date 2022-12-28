package com.konovus.apitesting.util

object Constants {
    const val TAG = "ApiTesting"
    const val BASE_URL_ALPHA_VANTAGE = "https://www.alphavantage.co"
    const val BASE_URL_YH_FINANCE = "https://yh-finance.p.rapidapi.com/"
    const val TEN_MINUTES = 10 * 60_000L

    val TIME_SPANS: List<Pair<String, Int>> = listOf(
        Pair("TIME_SERIES_INTRADAY", 16),
        Pair("TIME_SERIES_INTRADAY", 80),
        Pair("TIME_SERIES_DAILY_ADJUSTED", 30),
        Pair("TIME_SERIES_WEEKLY", 26),
        Pair("TIME_SERIES_WEEKLY", 52),
    )
}
