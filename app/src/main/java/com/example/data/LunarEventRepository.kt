package com.example.data

import kotlinx.coroutines.flow.Flow

class LunarEventRepository(private val dao: LunarEventDao) {
    val allEvents: Flow<List<LunarEvent>> = dao.getAllEvents()

    suspend fun insert(event: LunarEvent): Long = dao.insertEvent(event)

    suspend fun update(event: LunarEvent) = dao.updateEvent(event)

    suspend fun deleteById(id: Int) = dao.deleteEventById(id)
    
    suspend fun getById(id: Int): LunarEvent? = dao.getEventById(id)
}
