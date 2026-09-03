package com.ysmmfe.agora.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "task_completions",
    primaryKeys = ["itemId", "occurrenceDate"],
    foreignKeys = [
        ForeignKey(
            entity = ScheduleItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class TaskCompletionEntity(
    val itemId: Long,
    val occurrenceDate: String,
    val completedAt: Long = System.currentTimeMillis()
)
