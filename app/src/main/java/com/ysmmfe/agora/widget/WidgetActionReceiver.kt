package com.ysmmfe.agora.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ysmmfe.agora.data.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class WidgetActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE_TASK) return
        val id = intent.getLongExtra(EXTRA_ID, 0)
        val date = intent.getStringExtra(EXTRA_DATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()
        if (id <= 0) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ScheduleRepository(context).toggleTask(id, date)
                WidgetUpdater.refreshAll(context)
            } finally { pending.finish() }
        }
    }

    companion object {
        const val ACTION_TOGGLE_TASK = "com.ysmmfe.agora.TOGGLE_TASK"
        const val EXTRA_ID = "item_id"
        const val EXTRA_DATE = "occurrence_date"
    }
}
