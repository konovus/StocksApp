package com.konovus.apitesting.data.csv

import android.annotation.SuppressLint
import android.util.Log
import com.konovus.apitesting.data.local.entities.CompanyListing
import com.konovus.apitesting.data.local.entities.IntraDayInfo
import com.konovus.apitesting.util.Constants.TAG
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntradayParser @Inject constructor() : CSVParser<IntraDayInfo> {

    override suspend fun parse(stream: InputStream): List<IntraDayInfo> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .drop(1)
                .mapNotNull { line ->
                    val timestamp = line.getOrNull(0)
                    val close = line.getOrNull(4)
                    IntraDayInfo(
                        //todo
                        timestamp = timestamp ?: return@mapNotNull null,
                        close = close?.toDouble() ?: return@mapNotNull null
                    )
                }
                .also { csvReader.close() }
        }
    }
}