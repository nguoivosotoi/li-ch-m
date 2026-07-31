package com.example.data

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader

data class LunarDate(val day: Int, val month: Int, val year: Int, val isLeapMonth: Boolean)

object LunarDataStore {
    private var isLoaded = false
    private var lunarMap = mutableMapOf<String, LunarDate>()

    fun init(context: Context) {
        if (isLoaded) return
        try {
            val inputStream = context.assets.open("lunar_data.json")
            val reader = InputStreamReader(inputStream)
            val jsonString = reader.readText()
            reader.close()
            
            val jsonObject = JSONObject(jsonString)
            val years = jsonObject.keys()
            while (years.hasNext()) {
                val y = years.next()
                val yearObj = jsonObject.getJSONObject(y)
                val months = yearObj.keys()
                while (months.hasNext()) {
                    val m = months.next()
                    val monthArr = yearObj.getJSONArray(m)
                    for (i in 0 until monthArr.length()) {
                        val d = i + 1
                        val lunarArr = monthArr.getJSONArray(i)
                        val lDay = lunarArr.getInt(0)
                        val lMonth = lunarArr.getInt(1)
                        val lYear = lunarArr.getInt(2)
                        val isLeap = lunarArr.getInt(3) == 1
                        
                        lunarMap["$y-$m-$d"] = LunarDate(lDay, lMonth, lYear, isLeap)
                    }
                }
            }
            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isLoaded() = isLoaded

    fun getLunarDate(y: Int, m: Int, d: Int): LunarDate? {
        return lunarMap["$y-$m-$d"]
    }
    
    fun getSolarDate(lYear: Int, lMonth: Int, lDay: Int, isLeap: Boolean): String? {
        for ((solarDateStr, lunarDate) in lunarMap) {
            if (lunarDate.year == lYear && lunarDate.month == lMonth && lunarDate.day == lDay && lunarDate.isLeapMonth == isLeap) {
                return solarDateStr // format is "yyyy-MM-dd" (actually y-m-d)
            }
        }
        return null
    }
    
    // Find next 10 solar dates for a given lunar month and day
    fun findSolarDatesForLunar(lunarDay: Int, lunarMonth: Int, startYear: Int, count: Int = 10): List<String> {
        val results = mutableListOf<String>()
        var found = 0
        for (y in startYear..startYear + 20) {
            if (found >= count) break
            var yearFound = false
            for (m in 1..12) {
                for (d in 1..31) {
                    val key = "$y-$m-$d"
                    val lDate = lunarMap[key]
                    if (lDate != null && lDate.day == lunarDay && lDate.month == lunarMonth && !lDate.isLeapMonth) {
                        results.add(key) // Format: "YYYY-MM-DD"
                        yearFound = true
                        break
                    }
                }
                if (yearFound) break
            }
            if (yearFound) found++
        }
        return results
    }
}
