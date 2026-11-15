package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateHelper {
    private const val ISO_PATTERN = "yyyy-MM-dd"
    private const val DISPLAY_PATTERN = "MMMM d, yyyy"

    fun todayIso(): String {
        val fmt = SimpleDateFormat(ISO_PATTERN, Locale.US)
        return fmt.format(Calendar.getInstance().time)
    }

    fun millisToIso(millis: Long): String {
        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = millis
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    fun formatDisplayDate(iso: String): String {
        if (iso.isBlank()) return ""
        return try {
            val parser = SimpleDateFormat(ISO_PATTERN, Locale.US).apply { isLenient = false }
            val date = parser.parse(iso) ?: return iso
            SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault()).format(date)
        } catch (_: Exception) {
            iso
        }
    }

    fun daysSince(iso: String): Long? {
        if (iso.isBlank()) return null
        return try {
            val parser = SimpleDateFormat(ISO_PATTERN, Locale.US).apply { isLenient = false }
            val parsed = parser.parse(iso) ?: return null
            val startCal = zeroTime(Calendar.getInstance().apply { time = parsed })
            val todayCal = zeroTime(Calendar.getInstance())
            val diffMs = todayCal.timeInMillis - startCal.timeInMillis
            if (diffMs < 0) 0L else TimeUnit.MILLISECONDS.toDays(diffMs) + 1L
        } catch (_: Exception) {
            null
        }
    }

    fun daysUntilNextAnniversary(iso: String): Long? {
        if (iso.isBlank()) return null
        return try {
            val parser = SimpleDateFormat(ISO_PATTERN, Locale.US).apply { isLenient = false }
            val parsed = parser.parse(iso) ?: return null
            val sourceCal = Calendar.getInstance().apply { time = parsed }
            val todayCal = zeroTime(Calendar.getInstance())

            val targetMonth = sourceCal.get(Calendar.MONTH)
            val targetDay = sourceCal.get(Calendar.DAY_OF_MONTH)

            val nextCal = zeroTime(Calendar.getInstance()).apply {
                set(Calendar.YEAR, todayCal.get(Calendar.YEAR))
                set(Calendar.MONTH, targetMonth)
                val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, targetDay.coerceAtMost(maxDay))
            }

            if (nextCal.before(todayCal)) {
                nextCal.add(Calendar.YEAR, 1)
                val maxDay = nextCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                nextCal.set(Calendar.DAY_OF_MONTH, targetDay.coerceAtMost(maxDay))
            }

            val diffMs = nextCal.timeInMillis - todayCal.timeInMillis
            TimeUnit.MILLISECONDS.toDays(diffMs)
        } catch (_: Exception) {
            null
        }
    }

    private fun zeroTime(cal: Calendar): Calendar {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }
}
