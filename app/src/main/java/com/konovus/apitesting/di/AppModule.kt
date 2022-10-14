package com.konovus.apitesting.di

import android.app.Application
import androidx.room.Room
import com.konovus.apitesting.data.api.AlphaVantageApi
import com.konovus.apitesting.data.api.FinageApi
import com.konovus.apitesting.data.api.TwelveApi
import com.konovus.apitesting.data.local.dao.CompanyDao
import com.konovus.apitesting.data.local.dao.PortfolioDao
import com.konovus.apitesting.data.local.dao.StockDao
import com.konovus.apitesting.data.local.db.CompaniesDatabase
import com.konovus.apitesting.data.local.db.PortfolioDatabase
import com.konovus.apitesting.data.local.db.StockDatabase
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.util.Constants.BASE_URL
import com.konovus.apitesting.util.Constants.BASE_URL2
import com.konovus.apitesting.util.Constants.TWELVE_BASE_URL
import com.konovus.apitesting.util.NetworkConnectionObserver
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Provides
//    @Singleton
//    fun provideMoshi(): Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
//
//    @Provides
//    @Singleton
//    fun provideRetrofit(moshi: Moshi): Retrofit =
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .build()

    @Provides
    @Singleton
    fun provideAppStateStore(): Store<AppState> {
        return Store(AppState())
    }

    @Provides
    @Singleton
    fun provideFinageApi(): FinageApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FinageApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTwelveApi(): TwelveApi {
        return Retrofit.Builder()
            .baseUrl(TWELVE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TwelveApi::class.java)
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


    @Provides
    @Singleton
    fun provideStocksListingsApi(): AlphaVantageApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL2)
            .build()
            .create(AlphaVantageApi::class.java)
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
    fun providePortfolioDatabase(app: Application): PortfolioDatabase {
        return Room.databaseBuilder(
            app,
            PortfolioDatabase::class.java,
            "portfolios_db"
        )
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