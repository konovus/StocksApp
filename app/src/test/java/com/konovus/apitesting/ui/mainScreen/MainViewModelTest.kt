package com.konovus.apitesting.ui.mainScreen

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.konovus.apitesting.data.api.FakeYhFinanceApi
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.data.repository.FakeMainRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MainViewModelTest {

    @get:Rule
    var instantExecutor = InstantTaskExecutorRule()

    private lateinit var portfolioDatabase: PortfolioDatabase
    private lateinit var stocksDatabase: StockDatabase
    private lateinit var stocksDao: StockDao
    private lateinit var portfolioDao: PortfolioDao
    private lateinit var yhFinanceApi: FakeYhFinanceApi
    private lateinit var viewModel: MainViewModel
    private lateinit var repository: FakeMainRepository

    private val portfolio = Portfolio(1, "TDefault")
    private val stocks = mutableListOf<Stock>(
        Stock(1, isFavorite = true),
        Stock(2, isFavorite = false),
        Stock(3, isFavorite = true),
        Stock(4, isFavorite = false),
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        portfolioDatabase = Room
            .inMemoryDatabaseBuilder(context, PortfolioDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
        stocksDatabase = Room
            .inMemoryDatabaseBuilder(context, StockDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
        stocksDao = stocksDatabase.dao
        portfolioDao = portfolioDatabase.dao
        yhFinanceApi = FakeYhFinanceApi()
        repository = FakeMainRepository(stocksDao)
        viewModel = MainViewModel(repository = repository)

    }

    @After
    fun cleanup() {
        portfolioDatabase.close()

//        Dispatchers.resetMain()
//        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `createDefaultPortfolio, if no portfolio, creates portfolio` () = runTest{
        assertThat(portfolioDao.getPortfolioById(1)).isNull()
        portfolioDao.insertPortfolio(portfolio)
        assertThat(portfolioDao.getPortfolioById(1)).isEqualTo(portfolio)
    }

    @Test
    fun `collectPortfolio, dao returns one portfolio` () = runTest{
        portfolioDao.insertPortfolio(portfolio)

        portfolioDao.getPortfolioFlow().test {
            val result = awaitItem()
            assertThat(result).isEqualTo(portfolio)
        }
    }

    @Test
    fun `collectFavorites, receive the correct favorites list` () = runTest{
        assertThat(stocksDao.getTotalFavStocks()).isEqualTo(0)
        stocksDao.insertStocks(stocks)

    }




}