package com.ysmmfe.agora.ui

import android.content.Context
import com.ysmmfe.agora.R
import com.ysmmfe.agora.data.Category
import com.ysmmfe.agora.data.ScheduleItemEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object UiFormat {
    val locale: Locale = Locale("pt", "BR")

    fun longDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", locale))
            .replaceFirstChar { it.titlecase(locale) }

    fun shortWeekday(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replace(".", "").uppercase(locale)

    fun itemDetails(item: ScheduleItemEntity): String {
        val time = when {
            item.startTime == null -> "Sem horário"
            item.endTime != null -> "${item.startTime} – ${item.endTime}"
            else -> item.startTime
        }
        return if (item.notes.isBlank()) time else "$time · ${item.notes}"
    }

    fun categoryColor(context: Context, category: String): Int = context.getColor(
        when (Category.fromKey(category)) {
            Category.STUDY -> R.color.blue
            Category.WORK -> R.color.ice
            Category.PERSONAL -> R.color.pink
            Category.HEALTH -> R.color.coral
        }
    )
}
