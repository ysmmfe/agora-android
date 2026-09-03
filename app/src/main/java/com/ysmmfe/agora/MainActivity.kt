package com.ysmmfe.agora

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ysmmfe.agora.data.ItemKind
import com.ysmmfe.agora.data.ScheduleRepository
import com.ysmmfe.agora.ui.ScheduleAdapter
import com.ysmmfe.agora.ui.UiFormat
import com.ysmmfe.agora.widget.CompactWidgetProvider
import com.ysmmfe.agora.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: ScheduleRepository
    private lateinit var adapter: ScheduleAdapter
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repository = ScheduleRepository(this)
        adapter = ScheduleAdapter(
            onEdit = { openEditor(it.id, it.kind, selectedDate) },
            onToggle = { toggleTask(it.id) }
        )
        findViewById<RecyclerView>(R.id.schedule_list).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
        findViewById<TextView>(R.id.widgets_button).setOnClickListener { chooseWidget() }
        findViewById<TextView>(R.id.add_event_button).setOnClickListener {
            openEditor(0, ItemKind.EVENT.key, selectedDate)
        }
        findViewById<TextView>(R.id.add_task_button).setOnClickListener {
            openEditor(0, ItemKind.TASK.key, selectedDate)
        }
        findViewById<TextView>(R.id.today_label).text = UiFormat.longDate(LocalDate.now())
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun load() {
        val date = selectedDate
        val monday = date.minusDays((date.dayOfWeek.value - 1).toLong())
        scope.launch {
            val (agenda, week) = withContext(Dispatchers.IO) {
                repository.getAgenda(date) to repository.getWeek(monday)
            }
            if (date != selectedDate) return@launch
            findViewById<TextView>(R.id.selected_date_label).text = UiFormat.longDate(date)
            adapter.submit(agenda.events, agenda.tasks)
            renderWeek(monday, week.mapValues { it.value.size })
        }
    }

    private fun renderWeek(monday: LocalDate, counts: Map<LocalDate, Int>) {
        val container = findViewById<LinearLayout>(R.id.week_container)
        container.removeAllViews()
        repeat(7) { index ->
            val date = monday.plusDays(index.toLong())
            val selected = date == selectedDate
            val view = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(2.dp, 6.dp, 2.dp, 6.dp)
                background = GradientDrawable().apply {
                    cornerRadius = 10.dp.toFloat()
                    setColor(if (selected) this@MainActivity.getColor(R.color.day_selected) else Color.TRANSPARENT)
                }
                setOnClickListener { selectedDate = date; load() }
            }
            view.addView(dayText(UiFormat.shortWeekday(date), 11, selected, false))
            view.addView(dayText(date.dayOfMonth.toString(), 20, selected, true))
            view.addView(dayText("•".repeat((counts[date] ?: 0).coerceIn(0, 3)), 15, selected, false))
            container.addView(view, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
        }
    }

    private fun dayText(value: String, size: Int, selected: Boolean, bold: Boolean) = TextView(this).apply {
        text = value.ifEmpty { " " }
        textSize = size.toFloat()
        gravity = Gravity.CENTER
        setTextColor(getColor(if (selected) R.color.ink else R.color.white))
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun toggleTask(id: Long) {
        scope.launch {
            withContext(Dispatchers.IO) { repository.toggleTask(id, selectedDate) }
            WidgetUpdater.updateAll(this@MainActivity)
            load()
        }
    }

    private fun openEditor(id: Long, kind: String, date: LocalDate) {
        startActivity(Intent(this, EditItemActivity::class.java).apply {
            putExtra(EditItemActivity.EXTRA_ID, id)
            putExtra(EditItemActivity.EXTRA_KIND, kind)
            putExtra(EditItemActivity.EXTRA_DATE, date.toString())
        })
    }

    private fun chooseWidget() {
        AlertDialog.Builder(this)
            .setTitle("Adicionar widget")
            .setItems(arrayOf("Agenda completa", "Resumo compacto")) { _, which ->
                requestWidgetPin(if (which == 0) AgoraWidgetProvider::class.java else CompactWidgetProvider::class.java)
            }.show()
    }

    private fun requestWidgetPin(providerClass: Class<*>) {
        val manager = AppWidgetManager.getInstance(this)
        if (!manager.isRequestPinAppWidgetSupported) {
            Toast.makeText(this, R.string.pin_not_supported, Toast.LENGTH_LONG).show()
            return
        }
        val callback = PendingIntent.getActivity(
            this, providerClass.name.hashCode(), Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.requestPinAppWidget(ComponentName(this, providerClass), null, callback)
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
