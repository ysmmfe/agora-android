package com.ysmmfe.agora.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kind: String,
    val title: String,
    val notes: String = "",
    val category: String = Category.STUDY.key,
    val startDate: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val recurrence: String = Recurrence.NONE.key,
    val repeatDays: String = "",
    val recurrenceEndDate: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ItemKind(val key: String, val label: String) {
    EVENT("EVENT", "Compromisso"),
    TASK("TASK", "Tarefa");

    companion object {
        fun fromKey(key: String): ItemKind = entries.firstOrNull { it.key == key } ?: EVENT
    }
}

enum class Recurrence(val key: String, val label: String) {
    NONE("NONE", "Não se repete"),
    DAILY("DAILY", "Todos os dias"),
    WEEKLY("WEEKLY", "Dias da semana");

    companion object {
        fun fromKey(key: String): Recurrence = entries.firstOrNull { it.key == key } ?: NONE
    }
}

enum class Category(val key: String, val label: String) {
    STUDY("STUDY", "Estudos"),
    WORK("WORK", "Trabalho"),
    PERSONAL("PERSONAL", "Pessoal"),
    HEALTH("HEALTH", "Saúde");

    companion object {
        fun fromKey(key: String): Category = entries.firstOrNull { it.key == key } ?: STUDY
    }
}
