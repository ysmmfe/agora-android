package com.ysmmfe.agora

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class AgoraWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, createViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action?.let(REFRESH_ACTIONS::contains) == true) {
            val manager = AppWidgetManager.getInstance(context)
            val component = android.content.ComponentName(context, AgoraWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { widgetId ->
                manager.updateAppWidget(widgetId, createViews(context))
            }
        }
    }

    private fun createViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.agora_widget)
        val today = LocalDate.now()
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val locale = Locale("pt", "BR")

        views.setTextViewText(
            R.id.widget_heading,
            "HOJE · ${today.format(DateTimeFormatter.ofPattern("EEEE, d 'DE' MMMM", locale)).uppercase(locale)}"
        )

        DAY_VIEWS.forEachIndexed { index, dayView ->
            val date = monday.plusDays(index.toLong())
            views.setTextViewText(
                dayView.weekdayId,
                date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replace(".", "").uppercase(locale)
            )
            views.setTextViewText(dayView.dateId, date.dayOfMonth.toString())

            val isToday = date == today
            views.setInt(
                dayView.containerId,
                "setBackgroundResource",
                if (isToday) R.drawable.bg_day_selected else R.drawable.bg_day_clear
            )
            views.setTextColor(
                dayView.weekdayId,
                context.getColor(if (isToday) R.color.ink else R.color.white_70)
            )
            views.setTextColor(
                dayView.dateId,
                context.getColor(if (isToday) R.color.ink else R.color.white)
            )
        }

        val openApp = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        return views
    }

    private data class DayView(
        val containerId: Int,
        val weekdayId: Int,
        val dateId: Int
    )

    companion object {
        private val REFRESH_ACTIONS = setOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED
        )

        private val DAY_VIEWS = listOf(
            DayView(R.id.day_1, R.id.weekday_1, R.id.date_1),
            DayView(R.id.day_2, R.id.weekday_2, R.id.date_2),
            DayView(R.id.day_3, R.id.weekday_3, R.id.date_3),
            DayView(R.id.day_4, R.id.weekday_4, R.id.date_4),
            DayView(R.id.day_5, R.id.weekday_5, R.id.date_5),
            DayView(R.id.day_6, R.id.weekday_6, R.id.date_6),
            DayView(R.id.day_7, R.id.weekday_7, R.id.date_7)
        )
    }
}
