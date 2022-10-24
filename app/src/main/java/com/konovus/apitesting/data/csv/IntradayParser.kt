package com.konovus.apitesting.data.csv

import com.konovus.apitesting.data.local.entities.ChartData
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntradayParser @Inject constructor() : CSVParser<ChartData> {

    override suspend fun parse(stream: InputStream): List<ChartData> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .drop(1)
                .mapNotNull { line ->
                    val timestamp = line.getOrNull(0)
                    val close = line.getOrNull(4)
                    ChartData(
                        //todo
                        timestamp = timestamp ?: return@mapNotNull null,
                        close = close?.toDouble() ?: return@mapNotNull null
                    )
                }
                .also { csvReader.close() }
        }
    }
}