package com.example.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CalendarSyncManager
import com.example.data.LunarDataStore
import com.example.data.LunarEvent
import com.example.data.LunarEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(private val repository: LunarEventRepository, private val context: Context) : ViewModel() {

    init {
        LunarDataStore.init(context)
    }

    val allEvents: StateFlow<List<LunarEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _fontSizeMultiplier = MutableStateFlow(prefs.getFloat("font_scale", 1.0f))
    val fontSizeMultiplier: StateFlow<Float> = _fontSizeMultiplier

    fun setFontSizeMultiplier(scale: Float) {
        _fontSizeMultiplier.value = scale
        prefs.edit().putFloat("font_scale", scale).apply()
    }

    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth
    
    fun nextMonth() {
        val next = _currentMonth.value.clone() as Calendar
        next.add(Calendar.MONTH, 1)
        _currentMonth.value = next
    }
    
    fun prevMonth() {
        val prev = _currentMonth.value.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        _currentMonth.value = prev
    }

    fun addEvent(name: String, lunarDay: Int, lunarMonth: Int, isRecurring: Boolean) {
        viewModelScope.launch {
            val event = LunarEvent(name = name, lunarDay = lunarDay, lunarMonth = lunarMonth, isRecurring = isRecurring)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            // Sync to system calendar
            val systemIds = CalendarSyncManager.syncLunarEventToSystem(context, event, currentYear)
            
            val newEvent = event.copy(systemEventIds = systemIds)
            repository.insert(newEvent)
        }
    }

    fun deleteEvent(event: LunarEvent) {
        viewModelScope.launch {
            CalendarSyncManager.deleteEventsFromSystem(context, event.systemEventIds)
            repository.deleteById(event.id)
        }
    }
}

class MainViewModelFactory(
    private val database: AppDatabase,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(LunarEventRepository(database.lunarEventDao()), context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
