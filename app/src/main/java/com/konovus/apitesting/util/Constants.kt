package com.konovus.apitesting.util

object Constants {
    const val TAG = "ApiTesting"
    const val API_KEY = "API_KEY9bVEJ4M42Z4NHL24AM37FKZDBAQXZRNP"
    const val TWELVE_API_KEY = "43a182ca6cd7414daa66a4ba8eb1c5aa"
    const val API_KEY1 = "SPdILsBtZAQ8nWt5v5TN5gySPABNpci2SKt6wMP2"
    const val API_KEY_ALPHA_VANTAGE = "42OG87U8QU6I5T3O"
    const val API_KEY_YH_FINANCE = "eca80170damsh8d90d15e70ef8e1p1db4adjsn6f89cecc926b"
    const val BASE_URL = "https://api.finage.co.uk/"
    const val TWELVE_BASE_URL = "https://api.twelvedata.com/"
    const val BASE_URL1 = "https://yfapi.net"
    const val BASE_URL2 = "https://www.alphavantage.co"
    const val BASE_URL_YH_FINANCE = "https://yh-finance.p.rapidapi.com/"
    const val LAST_GAINERS_TIME = "LastGainersTime"
    const val LAST_PORTFOLIOS_BALANCE_TIME = "LastPortfoliosBalanceTime"
    const val TEN_MINUTES = 10 * 60_000

    val TIME_SPANS: List<Pair<String, Int>> = listOf(
        Pair("TIME_SERIES_INTRADAY", 16),
        Pair("TIME_SERIES_INTRADAY", 80),
        Pair("TIME_SERIES_DAILY", 30),
        Pair("TIME_SERIES_WEEKLY", 26),
        Pair("TIME_SERIES_WEEKLY", 52),

    )
}
