package com.konovus.apitesting.di

import android.app.Application
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.konovus.apitesting.data.api.AlphaVantageApi
import com.konovus.apitesting.data.api.YhFinanceApi
import com.konovus.apitesting.data.local.dao.CompanyDao
import com.konovus.apitesting.data.local.dao.PortfolioDao
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.db.CompaniesDatabase
import com.konovus.apitesting.data.local.db.PortfolioDatabase
import com.konovus.apitesting.data.local.db.StockDatabase
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.util.Constants.BASE_URL_ALPHA_VANTAGE
import com.konovus.apitesting.util.Constants.BASE_URL_YH_FINANCE
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.NetworkConnectionObserver
import com.konovus.apitesting.util.ioThread
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppStateStore(): Store<AppState> {
        return Store(AppState())
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


    @Provides
    @Singleton
    fun provideStocksListingsApi(): AlphaVantageApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL_ALPHA_VANTAGE)
            .build()
            .create(AlphaVantageApi::class.java)
    }

    @Provides
    @Singleton
    fun provideYhFinanceApi(): YhFinanceApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL_YH_FINANCE)
            .build()
            .create(YhFinanceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStocksDatabase(app: Application): StockDatabase {
        return Room.databaseBuilder(
            app,
            StockDatabase::class.java,
            "stocks_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCompanyDatabase(app: Application): CompaniesDatabase {
        return Room.databaseBuilder(
            app,
            CompaniesDatabase::class.java,
            "companies_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePortfolioDatabase(app: Application, portfolioDaoProvider: Provider<PortfolioDao>): PortfolioDatabase {
        return Room.databaseBuilder(
            app,
            PortfolioDatabase::class.java,
            "portfolios_db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                ioThread{
                    Log.i(TAG, "prepopulating Portfolio")
                    portfolioDaoProvider.get().prepopulatePortfolioDB(
                        Portfolio(name = "Default", id = 1, totalBalance = 100.0)
                    )
                }
            }
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)

                // Ensure there is always one game in the database
                // This will capture the case of the app storage
                // being cleared
                // This uses the existing instance, so the DB won't leak
                ioThread {
                    val dao = portfolioDaoProvider.get()

                    if (dao.portfolioCount() == 0) {
                        Log.i(TAG, "onOpen: inserting after Storage cleared")
                        dao.prepopulatePortfolioDB(
                            Portfolio(name = "Default", id = 1, totalBalance = 200.0)
                        )
                    }
                }
            }
        })
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideStockDao(db: StockDatabase): StockDao = db.dao

    @Provides
    @Singleton
    fun provideCompanyDao(db: CompaniesDatabase): CompanyDao = db.dao

    @Provides
    @Singleton
    fun providePortfolioDao(db: PortfolioDatabase): PortfolioDao = db.dao

    @Provides
    @Singleton
    fun provideNetworkConnectionObserver(
        app: Application
    ): NetworkConnectionObserver = NetworkConnectionObserver(app)

}