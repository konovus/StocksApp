package com.konovus.apitesting.data.local.entities

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {

    private val moshi: Moshi =  Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

        @TypeConverter
    fun fromCompanyInfoList(data: List<CompanyInfo>): String{
        val type = Types.newParameterizedType(List::class.java, CompanyInfo::class.java)
        val adapter = moshi.adapter<List<CompanyInfo>>(type)
        return adapter.toJson(data)
    }
    @TypeConverter
    fun toCompanyInfoList(json: String): List<CompanyInfo> {
        val type = Types.newParameterizedType(List::class.java, CompanyInfo::class.java)
        val adapter = moshi.adapter<List<CompanyInfo>>(type)
        return adapter.fromJson(json)!!
    }
        @TypeConverter
    fun fromStocksMap(data: MutableMap<String, Double>): String{
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Double::class.javaObjectType)
        val adapter = moshi.adapter<MutableMap<String, Double>>(type)
        return adapter.toJson(data)
    }
    @TypeConverter
    fun toStocksMap(json: String): MutableMap<String, Double> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Double::class.javaObjectType)
        val adapter = moshi.adapter<MutableMap<String, Double>>(type)
        return adapter.fromJson(json)!!
    }

        @TypeConverter
    fun fromTransactionList(data: List<Transaction>): String{
        val type = Types.newParameterizedType(List::class.java, Transaction::class.java)
        val adapter = moshi.adapter<List<Transaction>>(type)
        return adapter.toJson(data)
    }
    @TypeConverter
    fun toTransactionList(json: String): List<Transaction> {
        val type = Types.newParameterizedType(List::class.java, Transaction::class.java)
        val adapter = moshi.adapter<List<Transaction>>(type)
        return adapter.fromJson(json)!!
    }

    @TypeConverter
    fun fromChartChange(chartChange: Stock.ChartChange?): String {
        return moshi.adapter(Stock.ChartChange::class.java).toJson(chartChange)
    }

    @TypeConverter
    fun toChartChange(json: String): Stock.ChartChange? {
        return moshi.adapter(Stock.ChartChange::class.java).fromJson(json)
    }

    @TypeConverter
    fun fromChartOCHLStats(chartOCHLStats: Stock.ChartOCHLStats?): String {
        return moshi.adapter(Stock.ChartOCHLStats::class.java).toJson(chartOCHLStats)
    }

    @TypeConverter
    fun toChartOCHLStats(json: String): Stock.ChartOCHLStats? {
        return moshi.adapter(Stock.ChartOCHLStats::class.java).fromJson(json)
    }

    @TypeConverter
    fun fromDescriptionStats(descriptionStats: Stock.DescriptionStats?): String {
        return moshi.adapter(Stock.DescriptionStats::class.java).toJson(descriptionStats)
    }

    @TypeConverter
    fun toDescriptionStats(json: String): Stock.DescriptionStats? {
        return moshi.adapter(Stock.DescriptionStats::class.java).fromJson(json)
    }

//    @TypeConverter
//    fun fromIntraDayList(data: List<IntraDayInfo>): String{
//        val type = Types.newParameterizedType(List::class.java, String::class.java)
//        val adapter = moshi.adapter<List<IntraDayInfo>>(type)
//        return adapter.toJson(data)
//    }
//    @TypeConverter
//    fun toIntraDayList(json: String): List<IntraDayInfo> {
//        val type = Types.newParameterizedType(List::class.java, String::class.java)
//        val adapter = moshi.adapter<List<IntraDayInfo>>(type)
//        return adapter.fromJson(json)!!
//    }
}