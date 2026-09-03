package com.ysmmfe.agora.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ScheduleItemEntity::class, TaskCompletionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AgoraDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var instance: AgoraDatabase? = null

        fun getInstance(context: Context): AgoraDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AgoraDatabase::class.java,
                    "agora.db"
                ).build().also { instance = it }
            }
    }
}
