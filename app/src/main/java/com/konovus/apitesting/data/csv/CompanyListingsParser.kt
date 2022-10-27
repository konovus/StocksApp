package com.konovus.apitesting.data.csv

import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyListingsParser @Inject constructor() : CSVParser<CompanyInfo> {
    override suspend fun parse(stream: InputStream): List<CompanyInfo> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .drop(1)
                .mapNotNull { line ->
                    val symbol = line.getOrNull(0)
                    val name = line.getOrNull(1)
                    val exchange = line.getOrNull(2)
                    CompanyInfo(
                        name = name ?: "",
                        symbol = symbol ?: "",
                        exchange = exchange ?: "",
                    )
                }
                .also { csvReader.close() }
        }
    }
}