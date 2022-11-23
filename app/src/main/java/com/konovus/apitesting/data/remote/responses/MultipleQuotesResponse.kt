package com.konovus.apitesting.data.remote.responses

import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.local.models.Quote

data class MultipleQuotesResponse(
    val quoteResponse: QuoteResponse
) {
    data class QuoteResponse(
        val error: Any,
        val result: List<Result>
    ) {
        data class Result(
            val bookValue: Double,
            val dividendDate: Int,
            val dividendRate: Double,
            val dividendYield: Double,
            val dividendsPerShare: Double,
            val ebitda: Long,
            val epsCurrentYear: Double,
            val epsForward: Double,
            val epsNextQuarter: Double,
            val epsTrailingTwelveMonths: Double,
            val esgPopulated: Boolean,
            val exDividendDate: Int,
            val exchange: String,
            val exchangeTimezoneName: String,
            val exchangeTimezoneShortName: String,
            val fiftyDayAverage: Double,
            val fiftyDayAverageChange: Double,
            val fiftyDayAverageChangePercent: Double,
            val fiftyTwoWeekHigh: Double,
            val fiftyTwoWeekHighChange: Double,
            val fiftyTwoWeekHighChangePercent: Double,
            val fiftyTwoWeekLow: Double,
            val fiftyTwoWeekLowChange: Double,
            val fiftyTwoWeekLowChangePercent: Double,
            val fiftyTwoWeekRange: String,
            val firstTradeDateMilliseconds: Long,
            val floatShares: Long,
            val forwardPE: Double,
            val fullExchangeName: String,
            val heldPercentInsiders: Double,
            val heldPercentInstitutions: Double,
            val language: String,
            val longName: String,
            val market: String,
            val marketCap: Long,
            val marketState: String,
            val messageBoardId: String,
            val pegRatio: Double,
            val preMarketChange: Double,
            val preMarketChangePercent: Double,
            val preMarketPrice: Double,
            val preMarketTime: Int,
            val priceEpsCurrentYear: Double,
            val priceEpsNextQuarter: Double,
            val priceToBook: Double,
            val priceToSales: Double,
            val quoteSourceName: String,
            val quoteType: String,
            val region: String,
            val regularMarketChange: Double,
            val regularMarketChangePercent: Double,
            val regularMarketDayHigh: Double,
            val regularMarketDayLow: Double,
            val regularMarketDayRange: String,
            val regularMarketOpen: Double,
            val regularMarketPreviousClose: Double,
            val regularMarketPrice: Double,
            val regularMarketTime: Double,
            val regularMarketVolume: Double,
            val revenue: Long,
            val sharesOutstanding: Long,
            val sharesShort: Double,
            val sharesShortPrevMonth: Double,
            val shortName: String,
            val shortPercentFloat: Double,
            val shortRatio: Double,
            val sourceInterval: Double,
            val symbol: String,
            val targetPriceHigh: Double,
            val targetPriceLow: Double,
            val targetPriceMean: Double,
            val targetPriceMedian: Double,
            val totalCash: Long,
            val tradeable: Boolean,
            val trailingAnnualDividendRate: Double,
            val trailingAnnualDividendYield: Double,
            val trailingPE: Double,
            val triggerable: Boolean,
            val twoHundredDayAverage: Double,
            val twoHundredDayAverageChange: Double,
            val twoHundredDayAverageChangePercent: Double
        ) {
            fun toStock() = Stock(
                name = longName,
                symbol = symbol,
                price =  regularMarketPrice,
                change = regularMarketChange,
                changePercent = regularMarketChangePercent,
                lastUpdatedTime = System.currentTimeMillis()
            )

            fun toQuote() = Quote(
                name = longName,
                symbol = symbol,
                price =  regularMarketPrice.toString(),
                lastTimeUpdated = System.currentTimeMillis()
            )
        }
    }
}