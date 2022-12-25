package com.konovus.apitesting.data.local.entities

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PortfolioTest {

    private val portfolio = Portfolio(
        id = 1,
        name = "Default",
        totalBalance = 0.0,
        change = 0.0,
        changePercent = 0.0,
        transactions = emptyList(),
        lastUpdatedTime = 0
    )

    @Test
    fun `stocksToShareAmount, no transactions, empty stocksToShareAmount`() {
        assertThat(portfolio.stocksToShareAmount).isEmpty()
    }

    @Test
    fun `stocksToShareAmount, correct stocksToShareAmount`() {
        val transactions = listOf(
            Transaction(100.0, "AAPL", 50.0, 1234, OrderType.Buy)
        )

        val result = mapOf("AAPL" to 0.5, "DIS" to 2.0)

        assertThat(portfolio.stocksToShareAmount).isEqualTo(result)
    }


    @Test
    fun `stocksToShareAmount,buy100 and sell100, empty list`() {
        val transactions = listOf(
            Transaction(100.07, "AAPL", 100.0, 1234, OrderType.Buy),
            Transaction(100.07, "AAPL", 50.0, 1234, OrderType.Sell),
            Transaction(100.0, "AAL", 100.0, 1234, OrderType.Buy),
            Transaction(100.07, "AAPL", 50.0, 1234, OrderType.Sell),
        )

        assertThat(portfolio.copy(transactions = transactions).stocksToShareAmount).isEmpty()
    }

    @Test
    fun `stocksToShareAmount,buy100 and sell50, half a share`() {
        val transactions = listOf(
            Transaction(100.0, "AAPL", 100.0, 1234, OrderType.Buy),
            Transaction(100.0, "AAPL", 50.0, 1234, OrderType.Sell),
        )

        assertThat(portfolio.copy(transactions = transactions).stocksToShareAmount).isEqualTo(
            mapOf("AAPL" to 0.5)
        )
    }
}