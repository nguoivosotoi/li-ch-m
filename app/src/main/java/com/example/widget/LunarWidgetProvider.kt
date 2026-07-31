package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.LunarDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class LunarWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.lunar_widget_layout)
        
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        
        views.setTextViewText(R.id.widget_solar_date, d.toString())
        views.setTextViewText(R.id.widget_solar_month_year, "Tháng $m, $y")
        
        // Click on widget to open app
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_solar_date, pendingIntent)

        // Load Lunar data synchronously so process isn't killed
        if (!LunarDataStore.isLoaded()) {
            LunarDataStore.init(context)
        }
        val lunarDate = LunarDataStore.getLunarDate(y, m, d)
        
        if (lunarDate != null) {
            views.setTextViewText(R.id.widget_lunar_date, "Âm lịch: ${lunarDate.day}/${lunarDate.month}")
        } else {
            views.setTextViewText(R.id.widget_lunar_date, "Không có dữ liệu")
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
