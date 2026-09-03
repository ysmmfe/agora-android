package com.ysmmfe.agora.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_items ORDER BY startDate, startTime, title")
    suspend fun getAll(): List<ScheduleItemEntity>

    @Query("SELECT * FROM schedule_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScheduleItemEntity?

    @Query("SELECT COUNT(*) FROM schedule_items")
    suspend fun countItems(): Int

    @Upsert
    suspend fun upsert(item: ScheduleItemEntity): Long

    @Delete
    suspend fun delete(item: ScheduleItemEntity)

    @Query("SELECT itemId FROM task_completions WHERE occurrenceDate = :date")
    suspend fun completedItemIds(date: String): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM task_completions WHERE itemId = :itemId AND occurrenceDate = :date)")
    suspend fun isCompleted(itemId: Long, date: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markCompleted(completion: TaskCompletionEntity)

    @Query("DELETE FROM task_completions WHERE itemId = :itemId AND occurrenceDate = :date")
    suspend fun clearCompletion(itemId: Long, date: String)
}
