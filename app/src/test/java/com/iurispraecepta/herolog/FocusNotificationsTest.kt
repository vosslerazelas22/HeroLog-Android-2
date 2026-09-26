package com.iurispraecepta.herolog

import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.service.FocusNotifications
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.ServiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusNotificationsTest {

    private val context: android.content.Context =
        ApplicationProvider.getApplicationContext()

    @Test
    fun notificationIds_areFixedAndDistinct() {
        assertEquals(1001, FocusNotifications.ID_TIMER)
        assertEquals(1002, FocusNotifications.ID_COMPLETED)
        assertEquals(1003, FocusNotifications.ID_SUGGESTION)
    }

    @Test
    fun createChannels_registersSessionLowAndRewardsDefault() {
        FocusNotifications.createChannels(context)

        val manager =
            context.getSystemService(android.content.Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
        val session = manager.getNotificationChannel(FocusNotifications.CHANNEL_SESSION)
        val rewards = manager.getNotificationChannel(FocusNotifications.CHANNEL_REWARDS)

        assertEquals(
            android.app.NotificationManager.IMPORTANCE_LOW,
            session.importance
        )
        assertEquals(
            android.app.NotificationManager.IMPORTANCE_DEFAULT,
            rewards.importance
        )
    }

    @Test
    fun timerNotification_running_showsPauseAndStopActions() {
        val state = ServiceState(
            phase = ServicePhase.RUNNING,
            skillName = "Kotlin",
            endTimeMillis = System.currentTimeMillis() + 60_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertTrue(notification.actions.size == 2)
        assertEquals("Pausar", notification.actions[0].title.toString())
        assertEquals("Parar", notification.actions[1].title.toString())
    }

    @Test
    fun timerNotification_paused_showsResumeAction() {
        val state = ServiceState(
            phase = ServicePhase.PAUSED,
            skillName = "Kotlin",
            pausedRemainingMillis = 60_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals("Retomar", notification.actions[0].title.toString())
    }

    @Test
    fun completionNotification_showsXpAndGoldSummary() {
        val notification = FocusNotifications.completionNotification(context, xp = 60, gold = 75)

        val text = notification.extras.getCharSequence(android.app.Notification.EXTRA_TEXT).toString()
        assertTrue(text.contains("60"))
        assertTrue(text.contains("75"))
        assertEquals(2, notification.actions.size)
        assertEquals("Iniciar descanso", notification.actions[0].title.toString())
        assertEquals("Pular descanso", notification.actions[1].title.toString())
    }

    @Test
    fun suggestionNotification_showsConfiguredFocusMinutes() {
        val notification = FocusNotifications.suggestionNotification(context, focusMinutes = 25)

        val text = notification.extras.getCharSequence(android.app.Notification.EXTRA_TEXT).toString()
        assertTrue(text.contains("25"))
        assertEquals("Iniciar", notification.actions[0].title.toString())
    }
}
