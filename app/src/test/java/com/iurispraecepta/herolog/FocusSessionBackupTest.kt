package com.iurispraecepta.herolog

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.service.FocusSessionReceiver
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Importante #4: alarme inexato que dispara antes do fim real deve reagendar,
 * nunca descartar a conclusão.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusSessionBackupTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val receiver = FocusSessionReceiver()

    private fun alarmManager(): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun scheduledCount(): Int =
        Shadows.shadowOf(alarmManager()).scheduledAlarms.size

    private fun cancelScheduledAlarm() {
        val intent = Intent(context, FocusSessionReceiver::class.java).apply {
            action = FocusSessionReceiver.ACTION_BACKUP
        }
        val pending = PendingIntent.getBroadcast(
            context, 9001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager().cancel(pending)
    }

    @Test
    fun backupFiredEarly_reschedulesForRealEnd() = runTest {
        val realEnd = System.currentTimeMillis() + 600_000L
        FocusSessionReceiver.scheduleSessionBackup(context, realEnd)
        // Simula o disparo precoce: o alarme já consumido some da fila.
        cancelScheduledAlarm()

        receiver.handleBackup(context)

        assertEquals(1, scheduledCount())
        assertEquals(
            FocusSessionReceiver.KIND_SESSION,
            FocusSessionReceiver.pendingBackup(context).first
        )
    }

    @Test
    fun backupExpired_withEmptyDatabase_clearsWithoutCrash() = runTest {
        FocusSessionReceiver.scheduleSessionBackup(context, System.currentTimeMillis() - 1_000L)

        receiver.handleBackup(context)

        assertNull(FocusSessionReceiver.pendingBackup(context).first)
    }
}
