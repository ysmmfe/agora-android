package com.ysmmfe.agora

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ysmmfe.agora.data.Category
import com.ysmmfe.agora.data.ItemKind
import com.ysmmfe.agora.data.Recurrence
import com.ysmmfe.agora.data.ScheduleItemEntity
import com.ysmmfe.agora.data.ScheduleRepository
import com.ysmmfe.agora.ui.UiFormat
import com.ysmmfe.agora.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EditItemActivity : AppCompatActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: ScheduleRepository
    private var current: ScheduleItemEntity? = null
    private var startDate: LocalDate = LocalDate.now()
    private var endRepeatDate: LocalDate? = null
    private var startTime: LocalTime? = null
    private var endTime: LocalTime? = null

    private val kindSpinner by lazy { findViewById<Spinner>(R.id.kind_spinner) }
    private val categorySpinner by lazy { findViewById<Spinner>(R.id.category_spinner) }
    private val recurrenceSpinner by lazy { findViewById<Spinner>(R.id.recurrence_spinner) }
    private val dayChecks by lazy {
        listOf(R.id.day_1_check, R.id.day_2_check, R.id.day_3_check, R.id.day_4_check,
            R.id.day_5_check, R.id.day_6_check, R.id.day_7_check).map { findViewById<CheckBox>(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)
        repository = ScheduleRepository(this)
        startDate = intent.getStringExtra(EXTRA_DATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()
        setupSpinners()
        setupActions()

        val id = intent.getLongExtra(EXTRA_ID, 0)
        if (id > 0) loadItem(id) else {
            val kind = ItemKind.fromKey(intent.getStringExtra(EXTRA_KIND) ?: ItemKind.EVENT.key)
            kindSpinner.setSelection(kind.ordinal)
            dayChecks[startDate.dayOfWeek.value - 1].isChecked = true
            updateLabels()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun setupSpinners() {
        kindSpinner.adapter = spinnerAdapter(ItemKind.entries.map { it.label })
        categorySpinner.adapter = spinnerAdapter(Category.entries.map { it.label })
        recurrenceSpinner.adapter = spinnerAdapter(Recurrence.entries.map { it.label })
        recurrenceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateRecurrenceVisibility()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun spinnerAdapter(labels: List<String>) = ArrayAdapter(
        this, android.R.layout.simple_spinner_item, labels
    ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

    private fun setupActions() {
        findViewById<TextView>(R.id.start_date_button).setOnClickListener {
            pickDate(startDate) { startDate = it; updateLabels() }
        }
        findViewById<TextView>(R.id.start_time_button).setOnClickListener {
            pickTime(startTime ?: LocalTime.of(8, 0)) { startTime = it; updateLabels() }
        }
        findViewById<TextView>(R.id.end_time_button).setOnClickListener {
            pickTime(endTime ?: startTime?.plusHours(1) ?: LocalTime.of(9, 0)) { endTime = it; updateLabels() }
        }
        findViewById<TextView>(R.id.clear_time_button).setOnClickListener {
            startTime = null; endTime = null; updateLabels()
        }
        findViewById<CheckBox>(R.id.end_repeat_check).setOnCheckedChangeListener { _, checked ->
            findViewById<TextView>(R.id.end_repeat_button).visibility = if (checked) View.VISIBLE else View.GONE
            if (checked && endRepeatDate == null) endRepeatDate = startDate.plusMonths(3)
            updateLabels()
        }
        findViewById<TextView>(R.id.end_repeat_button).setOnClickListener {
            pickDate(endRepeatDate ?: startDate.plusMonths(3)) { endRepeatDate = it; updateLabels() }
        }
        findViewById<TextView>(R.id.save_button).setOnClickListener { save() }
        findViewById<TextView>(R.id.delete_button).setOnClickListener { confirmDelete() }
    }

    private fun loadItem(id: Long) {
        scope.launch {
            val item = withContext(Dispatchers.IO) { repository.getById(id) }
            if (item == null) { finish(); return@launch }
            current = item
            findViewById<TextView>(R.id.editor_heading).text = "Editar item"
            findViewById<EditText>(R.id.title_input).setText(item.title)
            findViewById<EditText>(R.id.notes_input).setText(item.notes)
            kindSpinner.setSelection(ItemKind.fromKey(item.kind).ordinal)
            categorySpinner.setSelection(Category.fromKey(item.category).ordinal)
            recurrenceSpinner.setSelection(Recurrence.fromKey(item.recurrence).ordinal)
            startDate = LocalDate.parse(item.startDate)
            startTime = item.startTime?.let { LocalTime.parse(it) }
            endTime = item.endTime?.let { LocalTime.parse(it) }
            item.repeatDays.split(',').mapNotNull(String::toIntOrNull).forEach {
                dayChecks.getOrNull(it - 1)?.isChecked = true
            }
            endRepeatDate = item.recurrenceEndDate?.let { LocalDate.parse(it) }
            findViewById<CheckBox>(R.id.end_repeat_check).isChecked = endRepeatDate != null
            findViewById<TextView>(R.id.delete_button).visibility = View.VISIBLE
            updateRecurrenceVisibility()
            updateLabels()
        }
    }

    private fun updateRecurrenceVisibility() {
        val recurrence = Recurrence.entries[recurrenceSpinner.selectedItemPosition.coerceAtLeast(0)]
        findViewById<LinearLayout>(R.id.weekday_container).visibility =
            if (recurrence == Recurrence.WEEKLY) View.VISIBLE else View.GONE
        findViewById<CheckBox>(R.id.end_repeat_check).visibility =
            if (recurrence == Recurrence.NONE) View.GONE else View.VISIBLE
        if (recurrence == Recurrence.NONE) findViewById<TextView>(R.id.end_repeat_button).visibility = View.GONE
    }

    private fun updateLabels() {
        findViewById<TextView>(R.id.start_date_button).text = UiFormat.longDate(startDate)
        findViewById<TextView>(R.id.start_time_button).text = startTime?.format(TIME_FORMAT) ?: "Início"
        findViewById<TextView>(R.id.end_time_button).text = endTime?.format(TIME_FORMAT) ?: "Fim"
        findViewById<TextView>(R.id.end_repeat_button).text =
            endRepeatDate?.let(UiFormat::longDate) ?: "Escolher data final"
    }

    private fun save() {
        val title = findViewById<EditText>(R.id.title_input).text.toString().trim()
        if (title.isEmpty()) {
            findViewById<EditText>(R.id.title_input).error = "Digite um título"
            return
        }
        if (endTime != null && startTime == null) {
            toast("Escolha primeiro o horário de início")
            return
        }
        val recurrence = Recurrence.entries[recurrenceSpinner.selectedItemPosition]
        val repeatDays = dayChecks.mapIndexedNotNull { index, check -> if (check.isChecked) index + 1 else null }
        if (recurrence == Recurrence.WEEKLY && repeatDays.isEmpty()) {
            toast("Escolha ao menos um dia da semana")
            return
        }
        val hasEnd = findViewById<CheckBox>(R.id.end_repeat_check).isChecked
        if (hasEnd && endRepeatDate?.isBefore(startDate) == true) {
            toast("A data final precisa ser posterior à inicial")
            return
        }
        val old = current
        val item = ScheduleItemEntity(
            id = old?.id ?: 0,
            kind = ItemKind.entries[kindSpinner.selectedItemPosition].key,
            title = title,
            notes = findViewById<EditText>(R.id.notes_input).text.toString().trim(),
            category = Category.entries[categorySpinner.selectedItemPosition].key,
            startDate = startDate.toString(),
            startTime = startTime?.format(TIME_FORMAT),
            endTime = endTime?.format(TIME_FORMAT),
            recurrence = recurrence.key,
            repeatDays = if (recurrence == Recurrence.WEEKLY) repeatDays.joinToString(",") else "",
            recurrenceEndDate = if (recurrence != Recurrence.NONE && hasEnd) endRepeatDate?.toString() else null,
            createdAt = old?.createdAt ?: System.currentTimeMillis()
        )
        scope.launch {
            withContext(Dispatchers.IO) { repository.save(item) }
            WidgetUpdater.updateAll(this@EditItemActivity)
            finish()
        }
    }

    private fun confirmDelete() {
        val item = current ?: return
        AlertDialog.Builder(this).setTitle("Excluir ${item.title}?")
            .setMessage("Esta ação remove também todas as próximas repetições.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                scope.launch {
                    withContext(Dispatchers.IO) { repository.delete(item) }
                    WidgetUpdater.updateAll(this@EditItemActivity)
                    finish()
                }
            }.show()
    }

    private fun pickDate(initial: LocalDate, result: (LocalDate) -> Unit) {
        DatePickerDialog(this, { _, y, m, d -> result(LocalDate.of(y, m + 1, d)) },
            initial.year, initial.monthValue - 1, initial.dayOfMonth).show()
    }

    private fun pickTime(initial: LocalTime, result: (LocalTime) -> Unit) {
        TimePickerDialog(this, { _, h, m -> result(LocalTime.of(h, m)) },
            initial.hour, initial.minute, true).show()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_ID = "item_id"
        const val EXTRA_KIND = "item_kind"
        const val EXTRA_DATE = "item_date"
        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
    }
}
