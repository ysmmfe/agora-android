package com.ysmmfe.agora.data

import android.content.Context
import com.ysmmfe.agora.domain.RecurrenceEngine
import java.time.LocalDate

data class TaskOccurrence(
    val item: ScheduleItemEntity,
    val completed: Boolean
)

data class DailyAgenda(
    val date: LocalDate,
    val events: List<ScheduleItemEntity>,
    val tasks: List<TaskOccurrence>
)

class ScheduleRepository(context: Context) {
    private val dao = AgoraDatabase.getInstance(context).scheduleDao()

    suspend fun getAgenda(date: LocalDate): DailyAgenda {
        val occurring = dao.getAll().filter { RecurrenceEngine.occursOn(it, date) }
        val completedIds = dao.completedItemIds(date.toString()).toSet()
        val events = RecurrenceEngine.sortForDay(occurring.filter { it.kind == ItemKind.EVENT.key })
        val tasks = RecurrenceEngine.sortForDay(occurring.filter { it.kind == ItemKind.TASK.key })
            .map { TaskOccurrence(it, it.id in completedIds) }
            .sortedWith(compareBy<TaskOccurrence> { it.completed }.thenBy { it.item.startTime ?: "99:99" })
        return DailyAgenda(date, events, tasks)
    }

    suspend fun getWeek(monday: LocalDate): Map<LocalDate, List<ScheduleItemEntity>> {
        val all = dao.getAll()
        return (0L..6L).associate { offset ->
            val date = monday.plusDays(offset)
            date to RecurrenceEngine.sortForDay(all.filter { RecurrenceEngine.occursOn(it, date) })
        }
    }

    suspend fun getById(id: Long): ScheduleItemEntity? = dao.getById(id)

    suspend fun save(item: ScheduleItemEntity): Long =
        dao.upsert(item.copy(updatedAt = System.currentTimeMillis()))

    suspend fun delete(item: ScheduleItemEntity) = dao.delete(item)

    suspend fun toggleTask(itemId: Long, date: LocalDate) {
        val value = date.toString()
        if (dao.isCompleted(itemId, value)) {
            dao.clearCompletion(itemId, value)
        } else {
            dao.markCompleted(TaskCompletionEntity(itemId, value))
        }
    }
}
