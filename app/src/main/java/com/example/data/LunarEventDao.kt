package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LunarEventDao {
    @Query("SELECT * FROM lunar_events ORDER BY id DESC")
    fun getAllEvents(): Flow<List<LunarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: LunarEvent): Long

    @Update
    suspend fun updateEvent(event: LunarEvent)

    @Query("DELETE FROM lunar_events WHERE id = :id")
    suspend fun deleteEventById(id: Int)
    
    @Query("SELECT * FROM lunar_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Int): LunarEvent?
}
