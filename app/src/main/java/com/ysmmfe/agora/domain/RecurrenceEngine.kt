package com.ysmmfe.agora.domain

import com.ysmmfe.agora.data.Recurrence
import com.ysmmfe.agora.data.ScheduleItemEntity
import java.time.LocalDate
import java.time.LocalTime

object RecurrenceEngine {
    fun occursOn(item: ScheduleItemEntity, date: LocalDate): Boolean {
        val start = item.startDate.toLocalDateOrNull() ?: return false
        val end = item.recurrenceEndDate?.toLocalDateOrNull()

        if (date.isBefore(start) || (end != null && date.isAfter(end))) return false

        return when (Recurrence.fromKey(item.recurrence)) {
            Recurrence.NONE -> date == start
            Recurrence.DAILY -> true
            Recurrence.WEEKLY -> {
                val selectedDays = item.repeatDays
                    .split(',')
                    .mapNotNull(String::toIntOrNull)
                    .toSet()
                    .ifEmpty { setOf(start.dayOfWeek.value) }
                date.dayOfWeek.value in selectedDays
            }
        }
    }

    fun sortForDay(items: List<ScheduleItemEntity>): List<ScheduleItemEntity> =
        items.sortedWith(
            compareBy<ScheduleItemEntity> { it.startTime?.toLocalTimeOrNull() ?: LocalTime.MAX }
                .thenBy { it.title.lowercase() }
        )

    private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
    private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this) }.getOrNull()
}
