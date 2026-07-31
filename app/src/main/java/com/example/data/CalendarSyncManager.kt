package com.example.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import java.util.Calendar
import java.util.TimeZone

object CalendarSyncManager {
    fun syncLunarEventToSystem(context: Context, event: LunarEvent, startYear: Int): String {
        // Delete old events
        deleteEventsFromSystem(context, event.systemEventIds)

        // Ensure Lunar data is loaded
        if (!LunarDataStore.isLoaded()) {
            LunarDataStore.init(context)
        }

        val solarDates = LunarDataStore.findSolarDatesForLunar(event.lunarDay, event.lunarMonth, startYear, if (event.isRecurring) 10 else 1)
        val newIds = mutableListOf<Long>()
        
        // We will insert into the primary calendar (ID = 1 for simplicity, or we can query for a valid calendar)
        val calendarId = getPrimaryCalendarId(context) ?: return ""

        for (dateStr in solarDates) {
            val parts = dateStr.split("-")
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()

            val cal = Calendar.getInstance()
            cal.set(y, m - 1, d, 8, 0, 0) // Remind at 8 AM
            val startMillis = cal.timeInMillis
            cal.set(y, m - 1, d, 9, 0, 0)
            val endMillis = cal.timeInMillis

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, event.name)
                put(CalendarContract.Events.DESCRIPTION, "Sự kiện Âm lịch: ${event.lunarDay}/${event.lunarMonth}")
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            try {
                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                val eventId = uri?.lastPathSegment?.toLongOrNull()
                if (eventId != null) {
                    newIds.add(eventId)
                    // Add reminder
                    val reminderValues = ContentValues().apply {
                        put(CalendarContract.Reminders.MINUTES, 60 * 24) // 1 day before
                        put(CalendarContract.Reminders.EVENT_ID, eventId)
                        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                    }
                    context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                    
                    val reminderValues2 = ContentValues().apply {
                        put(CalendarContract.Reminders.MINUTES, 0) // At time of event
                        put(CalendarContract.Reminders.EVENT_ID, eventId)
                        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                    }
                    context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues2)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
        return newIds.joinToString(",")
    }

    fun deleteEventsFromSystem(context: Context, idsStr: String) {
        if (idsStr.isBlank()) return
        val ids = idsStr.split(",")
        for (idStr in ids) {
            val id = idStr.toLongOrNull() ?: continue
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
            try {
                context.contentResolver.delete(uri, null, null)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun getPrimaryCalendarId(context: Context): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.IS_PRIMARY} = 1"
        try {
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection, selection, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getLong(0)
                }
            }
            
            // Fallback to any calendar
            val cursor2 = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection, null, null, null
            )
            cursor2?.use {
                if (it.moveToFirst()) {
                    return it.getLong(0)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        return null
    }
}
