package com.ysmmfe.agora

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))
        findViewById<TextView>(R.id.preview_date).text =
            LocalDate.now().format(formatter).replaceFirstChar { it.titlecase(Locale("pt", "BR")) }

        findViewById<TextView>(R.id.add_widget_button).setOnClickListener {
            requestWidgetPin()
        }
    }

    private fun requestWidgetPin() {
        val manager = AppWidgetManager.getInstance(this)
        if (!manager.isRequestPinAppWidgetSupported) {
            Toast.makeText(
                this,
                R.string.pin_not_supported,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val provider = ComponentName(this, AgoraWidgetProvider::class.java)
        val callbackIntent = Intent(this, MainActivity::class.java)
        val successCallback = PendingIntent.getActivity(
            this,
            0,
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        manager.requestPinAppWidget(provider, null, successCallback)
    }
}
