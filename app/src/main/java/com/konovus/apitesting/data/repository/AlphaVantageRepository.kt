package com.konovus.apitesting.data.repository

import android.util.Log
import androidx.paging.PagingSource
import com.konovus.apitesting.data.api.AlphaVantageApi
import com.konovus.apitesting.data.csv.CSVParser
import com.konovus.apitesting.data.local.db.CompaniesDatabase
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.data.local.entities.IntraDayInfo
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlphaVantageRepository @Inject constructor(
    private val api: AlphaVantageApi,
    companiesDatabase: CompaniesDatabase,
    private val csvParser: CSVParser<CompanyInfo>,
    private val csvParserIntraday: CSVParser<IntraDayInfo>
) {

    private val companyDao = companiesDatabase.dao

    private suspend fun getListingsFromApiToDb(): Resource<List<CompanyInfo>> {
        val remoteListings = try {
            val response = api.getListings()
            csvParser.parse(response.byteStream()).filterNot { it.name.lowercase().contains("etf") }
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error("Error: ${e.message}", null)
        }

        remoteListings.let { listings ->
            companyDao.clearCompanyInfos()
            companyDao.insertCompanyInfo(listings)
            return Resource.Success(listings)
        }
    }

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<PagingSource<Int, CompanyInfo>>> {
        return flow {
            Log.i(TAG, "getCompanyListings: fetch - $fetchFromRemote | query -$query, ${query.length}")
            emit(Resource.Loading(true))
            val localListings = companyDao.searchCompanyInfoPaged(query)
            emit(Resource.Success( data = localListings))

            val zeroRows = companyDao.getTotalRows() == 0
            val shouldJustLoadFromCache = !zeroRows && !fetchFromRemote
            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }

            when (val listings = getListingsFromApiToDb()) {
                is Resource.Error -> {
                    emit(Resource.Error(message = listings.message ?: "Error retrieving company listings"))
                    emit((Resource.Loading(false)))
                }
                is Resource.Loading -> emit((Resource.Loading(true)))
                is Resource.Success -> {
                    emit(Resource.Success(companyDao.searchCompanyInfoPaged(query)))
                    emit(Resource.Loading(false))
                }
            }
        }
    }

    suspend fun getIntradayInfo(
        symbol: String,
        function: Pair<String, Int>
    ): Resource<List<IntraDayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol = symbol, function = function.first )
            val results = csvParserIntraday.parse(response.byteStream())
            if (results.isEmpty())
                return Resource.Error("Max 5 calls per minute reached!")
            Collections.sort(results) { r1, r2 ->
                if (function.first.endsWith("INTRADAY"))
                    if (r1.toLocalDateTime().isBefore(r2.toLocalDateTime())) return@sort -1 else return@sort 1
                else if (r1.toLocalDate().isBefore(r2.toLocalDate())) return@sort -1 else return@sort 1
            }
            Resource.Success(results.takeLast(function.second))
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error("Couldn't load data.")
        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error("Couldn't load data.")
        }
    }
}