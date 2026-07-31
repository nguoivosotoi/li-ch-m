package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lunar_events")
data class LunarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val lunarDay: Int,
    val lunarMonth: Int,
    val isRecurring: Boolean = true,
    val systemEventIds: String = "" // comma separated
)
