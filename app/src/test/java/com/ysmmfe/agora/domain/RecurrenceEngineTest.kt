package com.ysmmfe.agora.domain

import com.ysmmfe.agora.data.ItemKind
import com.ysmmfe.agora.data.Recurrence
import com.ysmmfe.agora.data.ScheduleItemEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RecurrenceEngineTest {
    @Test fun singleItemOnlyOccursOnItsDate() {
        val item = item("2026-09-03")
        assertTrue(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-03")))
        assertFalse(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-04")))
    }

    @Test fun dailyItemRespectsEndDate() {
        val item = item("2026-09-01").copy(
            recurrence = Recurrence.DAILY.key,
            recurrenceEndDate = "2026-09-03"
        )
        assertTrue(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-02")))
        assertTrue(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-03")))
        assertFalse(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-04")))
    }

    @Test fun weeklyItemOccursOnlyOnSelectedWeekdays() {
        val item = item("2026-08-31").copy(
            recurrence = Recurrence.WEEKLY.key,
            repeatDays = "1,3,5"
        )
        assertTrue(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-02")))
        assertFalse(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-03")))
        assertTrue(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-04")))
    }

    @Test fun weeklyItemFallsBackToStartWeekday() {
        val item = item("2026-09-03").copy(recurrence = Recurrence.WEEKLY.key)
        assertTrue(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-10")))
        assertFalse(RecurrenceEngine.occursOn(item, LocalDate.parse("2026-09-11")))
    }

    private fun item(startDate: String) = ScheduleItemEntity(
        id = 1,
        kind = ItemKind.EVENT.key,
        title = "Teste",
        startDate = startDate
    )
}
