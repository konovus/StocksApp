package com.konovus.apitesting.di

import com.konovus.apitesting.data.csv.CSVParser
import com.konovus.apitesting.data.csv.CompanyListingsParser
import com.konovus.apitesting.data.csv.IntradayParser
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.data.local.entities.IntraDayInfo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCompanyListingsParser(
        companyListingsParser: CompanyListingsParser
    ): CSVParser<CompanyInfo>

    @Binds
    @Singleton
    abstract fun bindIntradayParser(
        intradayParser: IntradayParser
    ): CSVParser<IntraDayInfo>


}