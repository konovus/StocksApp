package com.konovus.apitesting.di

import android.app.Application
import androidx.room.Room
import com.konovus.apitesting.data.api.AlphaVantageApi
import com.konovus.apitesting.data.api.YhFinanceApi
import com.konovus.apitesting.data.local.dao.CompanyDao
import com.konovus.apitesting.data.local.dao.ProfileDao
import com.konovus.apitesting.data.local.db.CompaniesDatabase
import com.konovus.apitesting.data.local.db.ProfileDatabase
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.data.repository.IMainRepository
import com.konovus.apitesting.data.repository.MainRepository
import com.konovus.apitesting.util.Constants.BASE_URL_ALPHA_VANTAGE
import com.konovus.apitesting.util.Constants.BASE_URL_YH_FINANCE
import com.konovus.apitesting.util.NetworkConnectionObserver
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppStateStore(): Store<AppState> {
        return Store(AppState())
    }

    @Provides
    @Singleton
    fun provideMainRepository(
        yhFinanceApi: YhFinanceApi,
        profileDao: ProfileDao
    ): IMainRepository =
        MainRepository(yhFinanceApi, profileDao)


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
    fun provideProfileDatabase(app: Application): ProfileDatabase {
        return Room.databaseBuilder(
            app,
            ProfileDatabase::class.java,
            "profile_db"
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
    fun provideProfileDao(db: ProfileDatabase): ProfileDao = db.dao

    @Provides
    @Singleton
    fun provideCompanyDao(db: CompaniesDatabase): CompanyDao = db.dao

    @Provides
    @Singleton
    fun provideNetworkConnectionObserver(
        app: Application
    ): NetworkConnectionObserver = NetworkConnectionObserver(app)

}