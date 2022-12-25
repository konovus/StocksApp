package com.konovus.apitesting.data.local.dao

import com.konovus.apitesting.data.local.entities.Portfolio
import kotlinx.coroutines.flow.Flow

class FakePortfolioDao(var portfolios: List<Portfolio>? = mutableListOf()): PortfolioDao {

    override suspend fun insertPortfolio(portfolio: Portfolio) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePortfolio(portfolio: Portfolio) {
        TODO("Not yet implemented")
    }

    override fun getAllPortfolios(): Flow<List<Portfolio>> {
        TODO("Not yet implemented")
    }

    override suspend fun getPortfolioById(id: Int): Portfolio? {
        TODO("Not yet implemented")
    }
}