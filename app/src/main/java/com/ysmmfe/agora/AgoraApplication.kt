package com.ysmmfe.agora

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ysmmfe.agora.widget.WidgetUpdateWorker
import java.util.concurrent.TimeUnit

class AgoraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "agora-widget-refresh",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
