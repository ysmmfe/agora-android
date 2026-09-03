package com.ysmmfe.agora.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.ysmmfe.agora.AgoraWidgetProvider
import com.ysmmfe.agora.EditItemActivity
import com.ysmmfe.agora.MainActivity
import com.ysmmfe.agora.R
import com.ysmmfe.agora.data.DailyAgenda
import com.ysmmfe.agora.data.ScheduleItemEntity
import com.ysmmfe.agora.data.ScheduleRepository
import com.ysmmfe.agora.ui.UiFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object WidgetUpdater {
    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { refreshAll(appContext) }
    }

    suspend fun refreshAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        updateFull(context, manager.getAppWidgetIds(ComponentName(context, AgoraWidgetProvider::class.java)))
        updateCompact(context, manager.getAppWidgetIds(ComponentName(context, CompactWidgetProvider::class.java)))
    }

    suspend fun updateFull(context: Context, ids: IntArray) {
        if (ids.isEmpty()) return
        val today = LocalDate.now()
        val repository = ScheduleRepository(context)
        val agenda = repository.getAgenda(today)
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val week = repository.getWeek(monday)
        val manager = AppWidgetManager.getInstance(context)
        ids.forEach { manager.updateAppWidget(it, fullViews(context, agenda, week, monday)) }
    }

    suspend fun updateCompact(context: Context, ids: IntArray) {
        if (ids.isEmpty()) return
        val agenda = ScheduleRepository(context).getAgenda(LocalDate.now())
        val manager = AppWidgetManager.getInstance(context)
        ids.forEach { manager.updateAppWidget(it, compactViews(context, agenda)) }
    }

    private fun fullViews(
        context: Context,
        agenda: DailyAgenda,
        week: Map<LocalDate, List<ScheduleItemEntity>>,
        monday: LocalDate
    ): RemoteViews = RemoteViews(context.packageName, R.layout.agora_widget).apply {
        val today = agenda.date
        setOnClickPendingIntent(R.id.widget_root, openMain(context))
        setTextViewText(R.id.widget_heading, "HOJE · ${UiFormat.longDate(today).uppercase(UiFormat.locale)}")

        DAY_VIEWS.forEachIndexed { index, day ->
            val date = monday.plusDays(index.toLong())
            val isToday = date == today
            setTextViewText(day.weekday, UiFormat.shortWeekday(date))
            setTextViewText(day.date, date.dayOfMonth.toString())
            setInt(day.container, "setBackgroundResource", if (isToday) R.drawable.bg_day_selected else R.drawable.bg_day_clear)
            setTextColor(day.weekday, context.getColor(if (isToday) R.color.ink else R.color.white_70))
            setTextColor(day.date, context.getColor(if (isToday) R.color.ink else R.color.white))
            val items = week[date].orEmpty()
            day.bars.forEachIndexed { barIndex, barId ->
                val item = items.getOrNull(barIndex)
                setViewVisibility(barId, if (item == null) View.INVISIBLE else View.VISIBLE)
                if (item != null) setInt(barId, "setBackgroundColor", UiFormat.categoryColor(context, item.category))
            }
        }

        setViewVisibility(R.id.no_events, if (agenda.events.isEmpty()) View.VISIBLE else View.GONE)
        EVENT_VIEWS.forEachIndexed { index, row ->
            val item = agenda.events.getOrNull(index)
            setViewVisibility(row.container, if (item == null) View.GONE else View.VISIBLE)
            if (item != null) {
                setTextViewText(row.title, item.title)
                setTextViewText(row.time, "◷  ${timeLabel(item)}")
                setInt(row.accent, "setBackgroundColor", UiFormat.categoryColor(context, item.category))
                setOnClickPendingIntent(row.container, openEditor(context, item))
            }
        }
        val eventExtra = agenda.events.size - EVENT_VIEWS.size
        setViewVisibility(R.id.event_overflow, if (eventExtra > 0) View.VISIBLE else View.GONE)
        if (eventExtra > 0) setTextViewText(R.id.event_overflow, "+ $eventExtra compromisso(s)")

        setViewVisibility(R.id.no_tasks, if (agenda.tasks.isEmpty()) View.VISIBLE else View.GONE)
        TASK_VIEWS.forEachIndexed { index, id ->
            val task = agenda.tasks.getOrNull(index)
            setViewVisibility(id, if (task == null) View.GONE else View.VISIBLE)
            if (task != null) {
                setTextViewText(id, "${if (task.completed) "☑" else "☐"}  ${task.item.title}")
                setTextColor(id, context.getColor(if (task.completed) R.color.muted else R.color.white))
                setOnClickPendingIntent(id, toggleTask(context, task.item.id, today))
            }
        }
        val taskExtra = agenda.tasks.size - TASK_VIEWS.size
        setViewVisibility(R.id.task_overflow, if (taskExtra > 0) View.VISIBLE else View.GONE)
        if (taskExtra > 0) setTextViewText(R.id.task_overflow, "+ $taskExtra tarefa(s)")
    }

    private fun compactViews(context: Context, agenda: DailyAgenda): RemoteViews =
        RemoteViews(context.packageName, R.layout.compact_widget).apply {
            setOnClickPendingIntent(R.id.compact_root, openMain(context))
            setTextViewText(R.id.compact_date, "HOJE · ${UiFormat.longDate(agenda.date).uppercase(UiFormat.locale)}")
            val now = LocalTime.now()
            val next = agenda.events.firstOrNull { item ->
                item.startTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() }?.let { !it.isBefore(now) } ?: true
            }
            setTextViewText(R.id.compact_next, next?.title ?: "Agenda livre")
            setTextViewText(R.id.compact_next_time, next?.let(::timeLabel) ?: "Nenhum próximo compromisso")
            val pendingTasks = agenda.tasks.filterNot { it.completed }
            COMPACT_TASK_VIEWS.forEachIndexed { index, id ->
                val task = pendingTasks.getOrNull(index)
                setViewVisibility(id, if (task == null && index > 0) View.GONE else View.VISIBLE)
                if (task != null) {
                    setTextViewText(id, "☐  ${task.item.title}")
                    setOnClickPendingIntent(id, toggleTask(context, task.item.id, agenda.date))
                } else if (index == 0) setTextViewText(id, "Tudo concluído por hoje")
            }
        }

    private fun timeLabel(item: ScheduleItemEntity): String = when {
        item.startTime == null -> "Sem horário"
        item.endTime != null -> "${item.startTime} – ${item.endTime}"
        else -> item.startTime
    }

    private fun openMain(context: Context) = PendingIntent.getActivity(
        context, 1, Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun openEditor(context: Context, item: ScheduleItemEntity): PendingIntent {
        val intent = Intent(context, EditItemActivity::class.java).apply {
            putExtra(EditItemActivity.EXTRA_ID, item.id)
            data = Uri.parse("agora://item/${item.id}")
        }
        return PendingIntent.getActivity(context, item.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun toggleTask(context: Context, id: Long, date: LocalDate): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            action = WidgetActionReceiver.ACTION_TOGGLE_TASK
            putExtra(WidgetActionReceiver.EXTRA_ID, id)
            putExtra(WidgetActionReceiver.EXTRA_DATE, date.toString())
            data = Uri.parse("agora://task/$id/$date")
        }
        return PendingIntent.getBroadcast(context, (id xor date.toEpochDay()).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private data class DayView(val container: Int, val weekday: Int, val date: Int, val bars: List<Int>)
    private data class EventView(val container: Int, val accent: Int, val title: Int, val time: Int)

    private val DAY_VIEWS = listOf(
        DayView(R.id.day_1, R.id.weekday_1, R.id.date_1, listOf(R.id.bar_1_1, R.id.bar_1_2, R.id.bar_1_3)),
        DayView(R.id.day_2, R.id.weekday_2, R.id.date_2, listOf(R.id.bar_2_1, R.id.bar_2_2, R.id.bar_2_3)),
        DayView(R.id.day_3, R.id.weekday_3, R.id.date_3, listOf(R.id.bar_3_1, R.id.bar_3_2, R.id.bar_3_3)),
        DayView(R.id.day_4, R.id.weekday_4, R.id.date_4, listOf(R.id.bar_4_1, R.id.bar_4_2, R.id.bar_4_3)),
        DayView(R.id.day_5, R.id.weekday_5, R.id.date_5, listOf(R.id.bar_5_1, R.id.bar_5_2, R.id.bar_5_3)),
        DayView(R.id.day_6, R.id.weekday_6, R.id.date_6, listOf(R.id.bar_6_1, R.id.bar_6_2, R.id.bar_6_3)),
        DayView(R.id.day_7, R.id.weekday_7, R.id.date_7, listOf(R.id.bar_7_1, R.id.bar_7_2, R.id.bar_7_3))
    )
    private val EVENT_VIEWS = listOf(
        EventView(R.id.event_1, R.id.event_accent_1, R.id.event_title_1, R.id.event_time_1),
        EventView(R.id.event_2, R.id.event_accent_2, R.id.event_title_2, R.id.event_time_2),
        EventView(R.id.event_3, R.id.event_accent_3, R.id.event_title_3, R.id.event_time_3),
        EventView(R.id.event_4, R.id.event_accent_4, R.id.event_title_4, R.id.event_time_4)
    )
    private val TASK_VIEWS = listOf(R.id.task_1, R.id.task_2, R.id.task_3)
    private val COMPACT_TASK_VIEWS = listOf(R.id.compact_task_1, R.id.compact_task_2)
}
