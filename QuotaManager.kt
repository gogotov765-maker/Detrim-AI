package com.detrim.ai

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Хранит остаток квот в SharedPreferences.
 * Квоты сбрасываются на TOTAL_QUOTA при первом обращении в новый календарный день (00:00 по времени устройства).
 */
class QuotaManager(context: Context) {

    private val prefs = context.getSharedPreferences("detrim_quota", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    companion object {
        const val TOTAL_QUOTA = 65
        private const val KEY_USED = "quota_used"
        private const val KEY_DATE = "quota_date"
    }

    private fun todayString(): String = dateFormat.format(Date())

    private fun resetIfNewDay() {
        val storedDate = prefs.getString(KEY_DATE, null)
        val today = todayString()
        if (storedDate != today) {
            prefs.edit()
                .putString(KEY_DATE, today)
                .putInt(KEY_USED, 0)
                .apply()
        }
    }

    fun getRemaining(): Int {
        resetIfNewDay()
        val used = prefs.getInt(KEY_USED, 0)
        return (TOTAL_QUOTA - used).coerceAtLeast(0)
    }

    fun canAfford(cost: Int): Boolean = getRemaining() >= cost

    /** Списывает квоты, если хватает. Возвращает true при успехе. */
    fun spend(cost: Int): Boolean {
        resetIfNewDay()
        val used = prefs.getInt(KEY_USED, 0)
        if (TOTAL_QUOTA - used < cost) return false
        prefs.edit().putInt(KEY_USED, used + cost).apply()
        return true
    }
}
