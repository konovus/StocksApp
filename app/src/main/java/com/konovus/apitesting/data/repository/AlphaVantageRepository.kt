package com.konovus.apitesting.data.repository

import androidx.paging.PagingSource
import com.konovus.apitesting.data.api.AlphaVantageApi
import com.konovus.apitesting.data.csv.CSVParser
import com.konovus.apitesting.data.local.db.CompaniesDatabase
import com.konovus.apitesting.data.local.entities.ChartData
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.util.Resource
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
    private val csvParserIntraday: CSVParser<ChartData>
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

    suspend fun getCompanyListings(query: String): Resource<PagingSource<Int, CompanyInfo>> {
        return if (companyDao.getTotalRows() > 0)
            Resource.Success(companyDao.searchCompanyInfoPaged(query))
        else {
            val result = getListingsFromApiToDb()
            if (result is Resource.Success)
                Resource.Success(companyDao.searchCompanyInfoPaged())
            else Resource.Error(result.message.orEmpty(), null)
        }
    }

    suspend fun getChartData(
        symbol: String,
        function: Pair<String, Int>
    ): Resource<List<ChartData>> {
        return try {
            val response = api.getIntradayInfo(symbol = symbol, function = function.first )
            if (response.isSuccessful && response.body() != null) {
                val results = csvParserIntraday.parse(response.body()!!.byteStream())
                if (results.isEmpty())
                    return Resource.Error("Max 5 calls per minute reached.")
                Collections.sort(results) { r1, r2 ->
                    if (function.first.endsWith("INTRADAY"))
                        if (r1.toLocalDateTime().isBefore(r2.toLocalDateTime())) return@sort -1 else return@sort 1
                    else if (r1.toLocalDate().isBefore(r2.toLocalDate())) return@sort -1 else return@sort 1
                }
                Resource.Success(results.takeLast(function.second))
            } else Resource.Error("${response.code()}: ${response.message()}")
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error("Couldn't load data.")
        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error("Couldn't load data.")
        }
    }
}