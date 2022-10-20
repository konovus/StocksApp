package com.konovus.apitesting.data.remote.responses

import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.util.toNDecimals

data class StockSummaryResponse(
    val calendarEvents: CalendarEvents,
    val defaultKeyStatistics: DefaultKeyStatistics,
    val details: Details,
    val earnings: Earnings,
    val esgScores: EsgScores,
    val financialData: FinancialData,
    val financialsTemplate: FinancialsTemplate,
    val fundOwnership: FundOwnership,
    val insiderHolders: InsiderHolders,
    val insiderTransactions: InsiderTransactions,
    val institutionOwnership: InstitutionOwnership,
    val majorDirectHolders: MajorDirectHolders,
    val majorHoldersBreakdown: MajorHoldersBreakdown,
    val netSharePurchaseActivity: NetSharePurchaseActivity,
    val pageViews: PageViews,
    val price: Price,
    val quoteType: QuoteType,
    val recommendationTrend: RecommendationTrend,
    val summaryDetail: SummaryDetail,
    val summaryProfile: SummaryProfile,
    val symbol: String,
    val upgradeDowngradeHistory: UpgradeDowngradeHistory
) {

    fun toStock() =
        Stock(
            symbol = symbol,
            name = quoteType.shortName,
            price = price.regularMarketPrice.raw.toNDecimals(2),
            change = price.regularMarketChange.raw.toNDecimals(2),
            changePercent = price.regularMarketChangePercent.raw.toNDecimals(2),
            chartChange = Stock.ChartChange(
                change = price.regularMarketChange.raw.toNDecimals(2),
                changePercent = price.regularMarketChangePercent.raw.toNDecimals(2)),
            descriptionStats = Stock.DescriptionStats(
                exchange = price.exchangeName,
                industry = summaryProfile.industry,
                sector = summaryProfile.sector,
                employees = summaryProfile.fullTimeEmployees.toInt(),
                marketCap = summaryDetail.marketCap.fmt,
                description = summaryProfile.longBusinessSummary
            ),
            chartOCHLStats = Stock.ChartOCHLStats(
                open = price.regularMarketOpen.fmt,
                prevClose = price.regularMarketPreviousClose.fmt,
                high = price.regularMarketDayHigh.fmt,
                low = price.regularMarketDayLow.fmt,
                volume = price.regularMarketVolume.fmt
            ),
            lastUpdatedTime = System.currentTimeMillis()
        )

    data class CalendarEvents(
        val dividendDate: DividendDate,
        val earnings: Earnings,
        val exDividendDate: ExDividendDate,
        val maxAge: Double
    ) {
        class DividendDate

        data class Earnings(
            val earningsAverage: EarningsAverage,
            val earningsDate: List<EarningsDate>,
            val earningsHigh: EarningsHigh,
            val earningsLow: EarningsLow,
            val revenueAverage: RevenueAverage,
            val revenueHigh: RevenueHigh,
            val revenueLow: RevenueLow
        ) {
            data class EarningsAverage(
                val fmt: String,
                val raw: Double
            )

            data class EarningsDate(
                val fmt: String,
                val raw: Double
            )

            data class EarningsHigh(
                val fmt: String,
                val raw: Double
            )

            data class EarningsLow(
                val fmt: String,
                val raw: Double
            )

            data class RevenueAverage(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )

            data class RevenueHigh(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )

            data class RevenueLow(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )
        }

        class ExDividendDate
    }

    data class DefaultKeyStatistics(
        val `52WeekChange`: WeekChange,
        val annualHoldingsTurnover: AnnualHoldingsTurnover,
        val annualReportExpenseRatio: AnnualReportExpenseRatio,
        val beta: Beta,
        val beta3Year: Beta3Year,
        val bookValue: BookValue,
        val category: Any,
        val dateShortInterest: DateShortInterest,
        val earningsQuarterlyGrowth: EarningsQuarterlyGrowth,
        val enterpriseToEbitda: EnterpriseToEbitda,
        val enterpriseToRevenue: EnterpriseToRevenue,
        val enterpriseValue: EnterpriseValue,
        val fiveYearAverageReturn: FiveYearAverageReturn,
        val floatShares: FloatShares,
        val forwardEps: ForwardEps,
        val forwardPE: ForwardPE,
        val fundFamily: Any,
        val fundInceptionDate: FundInceptionDate,
        val heldPercentInsiders: HeldPercentInsiders,
        val heldPercentInstitutions: HeldPercentInstitutions,
        val impliedSharesOutstanding: ImpliedSharesOutstanding,
        val lastCapGain: LastCapGain,
        val lastDividendDate: LastDividendDate,
        val lastDividendValue: LastDividendValue,
        val lastFiscalYearEnd: LastFiscalYearEnd,
        val lastSplitDate: LastSplitDate,
        val lastSplitFactor: String,
        val legalType: Any,
        val maxAge: Double,
        val morningStarOverallRating: MorningStarOverallRating,
        val morningStarRiskRating: MorningStarRiskRating,
        val mostRecentQuarter: MostRecentQuarter,
        val netIncomeToCommon: NetIncomeToCommon,
        val nextFiscalYearEnd: NextFiscalYearEnd,
        val pegRatio: PegRatio,
        val priceHint: PriceHint,
        val priceToBook: PriceToBook,
        val priceToSalesTrailing12Months: PriceToSalesTrailing12Months,
        val profitMargins: ProfitMargins,
        val revenueQuarterlyGrowth: RevenueQuarterlyGrowth,
        val sharesOutstanding: SharesOutstanding,
        val sharesPercentSharesOut: SharesPercentSharesOut,
        val sharesShort: SharesShort,
        val sharesShortPreviousMonthDate: SharesShortPreviousMonthDate,
        val sharesShortPriorMonth: SharesShortPriorMonth,
        val shortPercentOfFloat: ShortPercentOfFloat,
        val shortRatio: ShortRatio,
        val threeYearAverageReturn: ThreeYearAverageReturn,
        val totalAssets: TotalAssets,
        val trailingEps: TrailingEps,
        val yield: Yield,
        val ytdReturn: YtdReturn
    ) {
        data class WeekChange(
            val fmt: String,
            val raw: Double
        )

        class AnnualHoldingsTurnover

        class AnnualReportExpenseRatio

        data class Beta(
            val fmt: String,
            val raw: Double
        )

        class Beta3Year

        data class BookValue(
            val fmt: String,
            val raw: Double
        )

        data class DateShortInterest(
            val fmt: String,
            val raw: Double
        )

        class EarningsQuarterlyGrowth

        data class EnterpriseToEbitda(
            val fmt: String,
            val raw: Double
        )

        data class EnterpriseToRevenue(
            val fmt: String,
            val raw: Double
        )

        data class EnterpriseValue(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        class FiveYearAverageReturn

        data class FloatShares(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class ForwardEps(
            val fmt: String,
            val raw: Double
        )

        data class ForwardPE(
            val fmt: String,
            val raw: Double
        )

        class FundInceptionDate

        data class HeldPercentInsiders(
            val fmt: String,
            val raw: Double
        )

        data class HeldPercentInstitutions(
            val fmt: String,
            val raw: Double
        )

        data class ImpliedSharesOutstanding(
            val fmt: Any,
            val longFmt: String,
            val raw: Double
        )

        class LastCapGain

        class LastDividendDate

        class LastDividendValue

        data class LastFiscalYearEnd(
            val fmt: String,
            val raw: Double
        )

        data class LastSplitDate(
            val fmt: String,
            val raw: Double
        )

        class MorningStarOverallRating

        class MorningStarRiskRating

        data class MostRecentQuarter(
            val fmt: String,
            val raw: Double
        )

        data class NetIncomeToCommon(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class NextFiscalYearEnd(
            val fmt: String,
            val raw: Double
        )

        data class PegRatio(
            val fmt: String,
            val raw: Double
        )

        data class PriceHint(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class PriceToBook(
            val fmt: String,
            val raw: Double
        )

        class PriceToSalesTrailing12Months

        data class ProfitMargins(
            val fmt: String,
            val raw: Double
        )

        class RevenueQuarterlyGrowth

        data class SharesOutstanding(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class SharesPercentSharesOut(
            val fmt: String,
            val raw: Double
        )

        data class SharesShort(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class SharesShortPreviousMonthDate(
            val fmt: String,
            val raw: Double
        )

        data class SharesShortPriorMonth(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        class ShortPercentOfFloat

        data class ShortRatio(
            val fmt: String,
            val raw: Double
        )

        class ThreeYearAverageReturn

        class TotalAssets

        data class TrailingEps(
            val fmt: String,
            val raw: Double
        )

        class Yield

        class YtdReturn
    }

    class Details

    data class Earnings(
        val earningsChart: EarningsChart,
        val financialCurrency: String,
        val financialsChart: FinancialsChart,
        val maxAge: Double
    ) {
        data class EarningsChart(
            val currentQuarterEstimate: CurrentQuarterEstimate,
            val currentQuarterEstimateDate: String,
            val currentQuarterEstimateYear: Double,
            val earningsDate: List<EarningsDate>,
            val quarterly: List<Quarterly>
        ) {
            data class CurrentQuarterEstimate(
                val fmt: String,
                val raw: Double
            )

            data class EarningsDate(
                val fmt: String,
                val raw: Double
            )

            data class Quarterly(
                val `actual`: Actual,
                val date: String,
                val estimate: Estimate
            ) {
                data class Actual(
                    val fmt: String,
                    val raw: Double
                )

                data class Estimate(
                    val fmt: String,
                    val raw: Double
                )
            }
        }

        data class FinancialsChart(
            val quarterly: List<Quarterly>,
            val yearly: List<Yearly>
        ) {
            data class Quarterly(
                val date: String,
                val earnings: Earnings,
                val revenue: Revenue
            ) {
                data class Earnings(
                    val fmt: String,
                    val longFmt: String,
                    val raw: Double
                )

                data class Revenue(
                    val fmt: String,
                    val longFmt: String,
                    val raw: Double
                )
            }

            data class Yearly(
                val date: Double,
                val earnings: Earnings,
                val revenue: Revenue
            ) {
                data class Earnings(
                    val fmt: String,
                    val longFmt: String,
                    val raw: Double
                )

                data class Revenue(
                    val fmt: String,
                    val longFmt: String,
                    val raw: Double
                )
            }
        }
    }

    class EsgScores

    data class FinancialData(
        val currentPrice: CurrentPrice,
        val currentRatio: CurrentRatio,
        val debtToEquity: DebtToEquity,
        val earningsGrowth: EarningsGrowth,
        val ebitda: Ebitda,
        val ebitdaMargins: EbitdaMargins,
        val financialCurrency: String,
        val freeCashflow: FreeCashflow,
        val grossMargins: GrossMargins,
        val grossProfits: GrossProfits,
        val maxAge: Double,
        val numberOfAnalystOpinions: NumberOfAnalystOpinions,
        val operatingCashflow: OperatingCashflow,
        val operatingMargins: OperatingMargins,
        val profitMargins: ProfitMargins,
        val quickRatio: QuickRatio,
        val recommendationKey: String,
        val recommendationMean: RecommendationMean,
        val returnOnAssets: ReturnOnAssets,
        val returnOnEquity: ReturnOnEquity,
        val revenueGrowth: RevenueGrowth,
        val revenuePerShare: RevenuePerShare,
        val targetHighPrice: TargetHighPrice,
        val targetLowPrice: TargetLowPrice,
        val targetMeanPrice: TargetMeanPrice,
        val targetMedianPrice: TargetMedianPrice,
        val totalCash: TotalCash,
        val totalCashPerShare: TotalCashPerShare,
        val totalDebt: TotalDebt,
        val totalRevenue: TotalRevenue
    ) {
        data class CurrentPrice(
            val fmt: String,
            val raw: Double
        )

        data class CurrentRatio(
            val fmt: String,
            val raw: Double
        )

        data class DebtToEquity(
            val fmt: String,
            val raw: Double
        )

        class EarningsGrowth

        data class Ebitda(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class EbitdaMargins(
            val fmt: String,
            val raw: Double
        )

        data class FreeCashflow(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class GrossMargins(
            val fmt: String,
            val raw: Double
        )

        data class GrossProfits(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class NumberOfAnalystOpinions(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class OperatingCashflow(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class OperatingMargins(
            val fmt: String,
            val raw: Double
        )

        data class ProfitMargins(
            val fmt: String,
            val raw: Double
        )

        data class QuickRatio(
            val fmt: String,
            val raw: Double
        )

        data class RecommendationMean(
            val fmt: String,
            val raw: Double
        )

        data class ReturnOnAssets(
            val fmt: String,
            val raw: Double
        )

        data class ReturnOnEquity(
            val fmt: String,
            val raw: Double
        )

        data class RevenueGrowth(
            val fmt: String,
            val raw: Double
        )

        data class RevenuePerShare(
            val fmt: String,
            val raw: Double
        )

        data class TargetHighPrice(
            val fmt: String,
            val raw: Double
        )

        data class TargetLowPrice(
            val fmt: String,
            val raw: Double
        )

        data class TargetMeanPrice(
            val fmt: String,
            val raw: Double
        )

        data class TargetMedianPrice(
            val fmt: String,
            val raw: Double
        )

        data class TotalCash(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class TotalCashPerShare(
            val fmt: String,
            val raw: Double
        )

        data class TotalDebt(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class TotalRevenue(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )
    }

    data class FinancialsTemplate(
        val code: String,
        val maxAge: Double
    )

    data class FundOwnership(
        val maxAge: Double,
        val ownershipList: List<Ownership>
    ) {
        data class Ownership(
            val maxAge: Double,
            val organization: String,
            val pctHeld: PctHeld,
            val position: Position,
            val reportDate: ReportDate,
            val value: Value
        ) {
            data class PctHeld(
                val fmt: String,
                val raw: Double
            )

            data class Position(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )

            data class ReportDate(
                val fmt: String,
                val raw: Double
            )

            data class Value(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )
        }
    }

    data class InsiderHolders(
        val holders: List<Holder>,
        val maxAge: Double
    ) {
        data class Holder(
            val latestTransDate: LatestTransDate,
            val maxAge: Double,
            val name: String,
            val positionDirect: PositionDirect,
            val positionDirectDate: PositionDirectDate,
            val positionIndirect: PositionIndirect,
            val positionIndirectDate: PositionIndirectDate,
            val relation: String,
            val transactionDescription: String,
            val url: String
        ) {
            data class LatestTransDate(
                val fmt: String,
                val raw: Double
            )

            data class PositionDirect(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )

            data class PositionDirectDate(
                val fmt: String,
                val raw: Double
            )

            data class PositionIndirect(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )

            data class PositionIndirectDate(
                val fmt: String,
                val raw: Double
            )
        }
    }

    data class InsiderTransactions(
        val maxAge: Double,
        val transactions: List<Transaction>
    ) {
        data class Transaction(
            val filerName: String,
            val filerRelation: String,
            val filerUrl: String,
            val maxAge: Double,
            val moneyText: String,
            val ownership: String,
            val shares: Shares,
            val startDate: StartDate,
            val transactionText: String,
            val value: Value
        ) {
            data class Shares(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )

            data class StartDate(
                val fmt: String,
                val raw: Double
            )

            data class Value(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )
        }
    }

    data class InstitutionOwnership(
        val maxAge: Double,
        val ownershipList: List<Ownership>
    ) {
        data class Ownership(
            val maxAge: Double,
            val organization: String,
            val pctHeld: PctHeld,
            val position: Position,
            val reportDate: ReportDate,
            val value: Value
        ) {
            data class PctHeld(
                val fmt: String,
                val raw: Double
            )

            data class Position(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )

            data class ReportDate(
                val fmt: String,
                val raw: Double
            )

            data class Value(
                val fmt: String,
                val longFmt: String,
                val raw: Double
            )
        }
    }

    data class MajorDirectHolders(
        val holders: List<Any>,
        val maxAge: Double
    )

    data class MajorHoldersBreakdown(
        val insidersPercentHeld: InsidersPercentHeld,
        val institutionsCount: InstitutionsCount,
        val institutionsFloatPercentHeld: InstitutionsFloatPercentHeld,
        val institutionsPercentHeld: InstitutionsPercentHeld,
        val maxAge: Double
    ) {
        data class InsidersPercentHeld(
            val fmt: String,
            val raw: Double
        )

        data class InstitutionsCount(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class InstitutionsFloatPercentHeld(
            val fmt: String,
            val raw: Double
        )

        data class InstitutionsPercentHeld(
            val fmt: String,
            val raw: Double
        )
    }

    data class NetSharePurchaseActivity(
        val buyInfoCount: BuyInfoCount,
        val buyInfoShares: BuyInfoShares,
        val buyPercentInsiderShares: BuyPercentInsiderShares,
        val maxAge: Double,
        val netInfoCount: NetInfoCount,
        val netInfoShares: NetInfoShares,
        val netPercentInsiderShares: NetPercentInsiderShares,
        val period: String,
        val sellInfoCount: SellInfoCount,
        val totalInsiderShares: TotalInsiderShares
    ) {
        data class BuyInfoCount(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class BuyInfoShares(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class BuyPercentInsiderShares(
            val fmt: String,
            val raw: Double
        )

        data class NetInfoCount(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class NetInfoShares(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class NetPercentInsiderShares(
            val fmt: String,
            val raw: Double
        )

        data class SellInfoCount(
            val fmt: Any,
            val longFmt: String,
            val raw: Double
        )

        data class TotalInsiderShares(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )
    }

    data class PageViews(
        val longTermTrend: String,
        val maxAge: Double,
        val midTermTrend: String,
        val shortTermTrend: String
    )

    data class Price(
        val averageDailyVolume10Day: AverageDailyVolume10Day,
        val averageDailyVolume3Month: AverageDailyVolume3Month,
        val circulatingSupply: CirculatingSupply,
        val currency: String,
        val currencySymbol: String,
        val exchange: String,
        val exchangeDataDelayedBy: Double,
        val exchangeName: String,
        val fromCurrency: Any,
        val lastMarket: Any,
        val longName: String,
        val marketCap: MarketCap,
        val marketState: String,
        val maxAge: Double,
        val openInterest: OpenInterest,
        val postMarketChange: PostMarketChange,
        val postMarketPrice: PostMarketPrice,
        val preMarketChange: PreMarketChange,
        val preMarketChangePercent: PreMarketChangePercent,
        val preMarketPrice: PreMarketPrice,
        val preMarketSource: String,
        val preMarketTime: Double,
        val priceHint: PriceHint,
        val quoteSourceName: String,
        val quoteType: String,
        val regularMarketChange: RegularMarketChange,
        val regularMarketChangePercent: RegularMarketChangePercent,
        val regularMarketDayHigh: RegularMarketDayHigh,
        val regularMarketDayLow: RegularMarketDayLow,
        val regularMarketOpen: RegularMarketOpen,
        val regularMarketPreviousClose: RegularMarketPreviousClose,
        val regularMarketPrice: RegularMarketPrice,
        val regularMarketSource: String,
        val regularMarketTime: Double,
        val regularMarketVolume: RegularMarketVolume,
        val shortName: String,
        val strikePrice: StrikePrice,
        val symbol: String,
        val toCurrency: Any,
        val underlyingSymbol: Any,
        val volume24Hr: Volume24Hr,
        val volumeAllCurrencies: VolumeAllCurrencies
    ) {
        data class AverageDailyVolume10Day(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class AverageDailyVolume3Month(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        class CirculatingSupply

        data class MarketCap(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        class OpenInterest

        class PostMarketChange

        class PostMarketPrice

        data class PreMarketChange(
            val fmt: String,
            val raw: Double
        )

        data class PreMarketChangePercent(
            val fmt: String,
            val raw: Double
        )

        data class PreMarketPrice(
            val fmt: String,
            val raw: Double
        )

        data class PriceHint(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class RegularMarketChange(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketChangePercent(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketDayHigh(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketDayLow(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketOpen(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketPreviousClose(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketPrice(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketVolume(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        class StrikePrice

        class Volume24Hr

        class VolumeAllCurrencies
    }

    data class QuoteType(
        val exchange: String,
        val exchangeTimezoneName: String,
        val exchangeTimezoneShortName: String,
        val gmtOffSetMilliseconds: String,
        val isEsgPopulated: Boolean,
        val longName: String,
        val market: String,
        val messageBoardId: String,
        val quoteType: String,
        val shortName: String,
        val symbol: String
    )

    data class RecommendationTrend(
        val maxAge: Double,
        val trend: List<Trend>
    ) {
        data class Trend(
            val buy: Double,
            val hold: Double,
            val period: String,
            val sell: Double,
            val strongBuy: Double,
            val strongSell: Double
        )
    }

    data class SummaryDetail(
        val algorithm: Any,
        val ask: Ask,
        val askSize: AskSize,
        val averageDailyVolume10Day: AverageDailyVolume10Day,
        val averageVolume: AverageVolume,
        val averageVolume10days: AverageVolume10days,
        val beta: Beta,
        val bid: Bid,
        val bidSize: BidSize,
        val circulatingSupply: CirculatingSupply,
        val currency: String,
        val dayHigh: DayHigh,
        val dayLow: DayLow,
        val dividendRate: DividendRate,
        val dividendYield: DividendYield,
        val exDividendDate: ExDividendDate,
        val expireDate: ExpireDate,
        val fiftyDayAverage: FiftyDayAverage,
        val fiftyTwoWeekHigh: FiftyTwoWeekHigh,
        val fiftyTwoWeekLow: FiftyTwoWeekLow,
        val fiveYearAvgDividendYield: FiveYearAvgDividendYield,
        val forwardPE: ForwardPE,
        val fromCurrency: Any,
        val lastMarket: Any,
        val marketCap: MarketCap,
        val maxAge: Double,
        val maxSupply: MaxSupply,
        val navPrice: NavPrice,
        val `open`: Open,
        val openInterest: OpenInterest,
        val payoutRatio: PayoutRatio,
        val previousClose: PreviousClose,
        val priceHint: PriceHint,
        val priceToSalesTrailing12Months: PriceToSalesTrailing12Months,
        val regularMarketDayHigh: RegularMarketDayHigh,
        val regularMarketDayLow: RegularMarketDayLow,
        val regularMarketOpen: RegularMarketOpen,
        val regularMarketPreviousClose: RegularMarketPreviousClose,
        val regularMarketVolume: RegularMarketVolume,
        val startDate: StartDate,
        val strikePrice: StrikePrice,
        val toCurrency: Any,
        val totalAssets: TotalAssets,
        val tradeable: Boolean,
        val trailingAnnualDividendRate: TrailingAnnualDividendRate,
        val trailingAnnualDividendYield: TrailingAnnualDividendYield,
        val twoHundredDayAverage: TwoHundredDayAverage,
        val volume: Volume,
        val volume24Hr: Volume24Hr,
        val volumeAllCurrencies: VolumeAllCurrencies,
        val yield: Yield,
        val ytdReturn: YtdReturn
    ) {
        data class Ask(
            val fmt: String,
            val raw: Double
        )

        data class AskSize(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class AverageDailyVolume10Day(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class AverageVolume(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class AverageVolume10days(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class Beta(
            val fmt: String,
            val raw: Double
        )

        data class Bid(
            val fmt: String,
            val raw: Double
        )

        data class BidSize(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        class CirculatingSupply

        data class DayHigh(
            val fmt: String,
            val raw: Double
        )

        data class DayLow(
            val fmt: String,
            val raw: Double
        )

        class DividendRate

        class DividendYield

        class ExDividendDate

        class ExpireDate

        data class FiftyDayAverage(
            val fmt: String,
            val raw: Double
        )

        data class FiftyTwoWeekHigh(
            val fmt: String,
            val raw: Double
        )

        data class FiftyTwoWeekLow(
            val fmt: String,
            val raw: Double
        )

        class FiveYearAvgDividendYield

        data class ForwardPE(
            val fmt: String,
            val raw: Double
        )

        data class MarketCap(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        class MaxSupply

        class NavPrice

        data class Open(
            val fmt: String,
            val raw: Double
        )

        class OpenInterest

        data class PayoutRatio(
            val fmt: String,
            val raw: Double
        )

        data class PreviousClose(
            val fmt: String,
            val raw: Double
        )

        data class PriceHint(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        data class PriceToSalesTrailing12Months(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketDayHigh(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketDayLow(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketOpen(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketPreviousClose(
            val fmt: String,
            val raw: Double
        )

        data class RegularMarketVolume(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        class StartDate

        class StrikePrice

        class TotalAssets

        data class TrailingAnnualDividendRate(
            val fmt: String,
            val raw: Double
        )

        data class TrailingAnnualDividendYield(
            val fmt: String,
            val raw: Double
        )

        data class TwoHundredDayAverage(
            val fmt: String,
            val raw: Double
        )

        data class Volume(
            val fmt: String,
            val longFmt: String,
            val raw: Double
        )

        class Volume24Hr

        class VolumeAllCurrencies

        class Yield

        class YtdReturn
    }

    data class SummaryProfile(
        val address1: String,
        val address2: String,
        val city: String,
        val companyOfficers: List<Any>,
        val country: String,
        val fullTimeEmployees: Double,
        val industry: String,
        val longBusinessSummary: String,
        val maxAge: Double,
        val phone: String,
        val sector: String,
        val website: String,
        val zip: String
    )

    data class UpgradeDowngradeHistory(
        val history: List<History>,
        val maxAge: Double
    ) {
        data class History(
            val action: String,
            val epochGradeDate: Double,
            val firm: String,
            val fromGrade: String,
            val toGrade: String
        )
    }
}