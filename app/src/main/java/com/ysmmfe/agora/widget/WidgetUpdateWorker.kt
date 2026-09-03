package com.ysmmfe.agora.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        WidgetUpdater.refreshAll(applicationContext)
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }
}
