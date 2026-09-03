package com.ysmmfe.agora.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ysmmfe.agora.R
import com.ysmmfe.agora.data.ScheduleItemEntity
import com.ysmmfe.agora.data.TaskOccurrence

sealed interface ScheduleRow {
    data class Header(val title: String) : ScheduleRow
    data class Item(val value: ScheduleItemEntity, val completed: Boolean = false) : ScheduleRow
    data class Empty(val message: String) : ScheduleRow
}

class ScheduleAdapter(
    private val onEdit: (ScheduleItemEntity) -> Unit,
    private val onToggle: (ScheduleItemEntity) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var rows: List<ScheduleRow> = emptyList()

    fun submit(agendaEvents: List<ScheduleItemEntity>, tasks: List<TaskOccurrence>) {
        rows = buildList {
            add(ScheduleRow.Header("COMPROMISSOS"))
            if (agendaEvents.isEmpty()) add(ScheduleRow.Empty("Nenhum compromisso neste dia"))
            else agendaEvents.forEach { add(ScheduleRow.Item(it)) }
            add(ScheduleRow.Header("TAREFAS"))
            if (tasks.isEmpty()) add(ScheduleRow.Empty("Nenhuma tarefa neste dia"))
            else tasks.forEach { add(ScheduleRow.Item(it.item, it.completed)) }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (rows[position]) {
        is ScheduleRow.Header -> 0
        is ScheduleRow.Item -> 1
        is ScheduleRow.Empty -> 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> HeaderHolder(inflater.inflate(R.layout.row_section_header, parent, false))
            1 -> ItemHolder(inflater.inflate(R.layout.row_schedule_item, parent, false))
            else -> EmptyHolder(inflater.inflate(R.layout.row_empty, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is ScheduleRow.Header -> (holder as HeaderHolder).text.text = row.title
            is ScheduleRow.Empty -> (holder as EmptyHolder).text.text = row.message
            is ScheduleRow.Item -> (holder as ItemHolder).bind(row)
        }
    }

    override fun getItemCount(): Int = rows.size

    private class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.section_title)
    }

    private class EmptyHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.empty_text)
    }

    private inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val bar: TextView = view.findViewById(R.id.category_bar)
        private val checkbox: CheckBox = view.findViewById(R.id.task_checkbox)
        private val title: TextView = view.findViewById(R.id.item_title)
        private val details: TextView = view.findViewById(R.id.item_details)

        fun bind(row: ScheduleRow.Item) {
            val item = row.value
            bar.setBackgroundColor(UiFormat.categoryColor(itemView.context, item.category))
            title.text = item.title
            details.text = UiFormat.itemDetails(item)
            checkbox.visibility = if (item.kind == "TASK") View.VISIBLE else View.GONE
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = row.completed
            title.paintFlags = if (row.completed) title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            checkbox.setOnCheckedChangeListener { _, _ -> onToggle(item) }
            itemView.setOnClickListener { onEdit(item) }
        }
    }
}
