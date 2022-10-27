package com.konovus.apitesting.util

object Constants {
    const val TAG = "ApiTesting"
    const val API_KEY_ALPHA_VANTAGE = "42OG87U8QU6I5T3O"
    const val API_KEY_YH_FINANCE = "5a4fd5ec9cmshdebe0579c0a3547p14fe6fjsn8893c29f58c7"
    const val BASE_URL2 = "https://www.alphavantage.co"
    const val BASE_URL_YH_FINANCE = "https://yh-finance.p.rapidapi.com/"
    const val TEN_MINUTES = 10 * 60_000

    val TIME_SPANS: List<Pair<String, Int>> = listOf(
        Pair("TIME_SERIES_INTRADAY", 16),
        Pair("TIME_SERIES_INTRADAY", 80),
        Pair("TIME_SERIES_DAILY", 30),
        Pair("TIME_SERIES_WEEKLY", 26),
        Pair("TIME_SERIES_WEEKLY", 52),

    )
}
