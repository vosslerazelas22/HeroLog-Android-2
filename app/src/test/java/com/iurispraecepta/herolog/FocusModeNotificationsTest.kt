package com.iurispraecepta.herolog

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.logic.focus.CognitiveDeathResult
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.service.FocusNotificationTheme
import com.iurispraecepta.herolog.service.FocusNotifications
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.ServiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Builders de notificação por modo (spec notificações por modo, T8–T13).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusModeNotificationsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun runningState(config: FocusSessionConfig, endTime: Long): ServiceState =
        ServiceState(
            phase = ServicePhase.RUNNING,
            sessionConfig = config,
            skillName = "Kotlin",
            durationMinutes = 25,
            endTimeMillis = endTime
        )

    private fun textOf(notification: Notification): String =
        notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString()

    @Test
    fun timerNotification_standard_showsGoldAccentFocusIconAndDuration() {
        val endTime = System.currentTimeMillis() + 25 * 60_000L
        val state = runningState(
            FocusSessionConfig(0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0),
            endTime
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals(FocusNotificationTheme.ACCENT_STANDARD, notification.color)
        assertEquals(R.drawable.ic_focus_small, notification.smallIcon.resId)
        assertEquals("Modo Padrão · 25 min", textOf(notification))
        assertEquals(endTime, notification.`when`)
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER))
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN))
    }

    @Test
    fun timerNotification_dungeon_showsPurpleAccentAndProgress() {
        val endTime = System.currentTimeMillis() + 60_000L
        val state = runningState(
            FocusSessionConfig(0, isWildernessChecked = false, isDungeonMode = true, dungeonSessions = 2),
            endTime
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals(FocusNotificationTheme.ACCENT_DUNGEON, notification.color)
        assertEquals(R.drawable.ic_focus_dungeon, notification.smallIcon.resId)
        // Mesma regra da tela (`RaidModeInfoBox`): contagem ANTES da sessão atual.
        assertEquals("Masmorra · 2/4", textOf(notification))
        assertEquals(endTime, notification.`when`)
    }

    @Test
    fun timerNotification_wildernessRunning_hasNoActionsAndStayText() {
        val endTime = System.currentTimeMillis() + 60_000L
        val state = runningState(
            FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0),
            endTime
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals(FocusNotificationTheme.ACCENT_WILDERNESS, notification.color)
        assertEquals(R.drawable.ic_focus_wilderness, notification.smallIcon.resId)
        assertTrue(notification.actions.isNullOrEmpty())
        assertEquals("Permaneça no app para manter o bônus", textOf(notification))
        assertEquals(endTime, notification.`when`)
    }

    @Test
    fun timerNotification_wildernessPaused_offersOnlyResume() {
        val state = ServiceState(
            phase = ServicePhase.PAUSED,
            sessionConfig = FocusSessionConfig(0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0),
            skillName = "Kotlin",
            pausedRemainingMillis = 60_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals(1, notification.actions.size)
        assertEquals("Retomar", notification.actions[0].title.toString())
        assertTrue(textOf(notification).startsWith("Pausado"))
    }

    @Test
    fun timerNotification_paused_showsFrozenTimeWithoutChronometer() {
        val state = ServiceState(
            phase = ServicePhase.PAUSED,
            sessionConfig = FocusSessionConfig(0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0),
            skillName = "Kotlin",
            pausedRemainingMillis = 60_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals("Pausado · 1:00 restantes", textOf(notification))
        assertTrue(!notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER))
        assertEquals(2, notification.actions.size)
        assertEquals("Retomar", notification.actions[0].title.toString())
        assertEquals("Abandonar", notification.actions[1].title.toString())
    }

    @Test
    fun breakNotification_showsEmeraldAccentSkipActionAndNoCycle() {
        val endTime = System.currentTimeMillis() + 5 * 60_000L
        val state = ServiceState(
            phase = ServicePhase.BREAK_RUNNING,
            breakEndTimeMillis = endTime,
            breakDurationMinutes = 5,
            breakTotalSeconds = 300
        )

        val notification = FocusNotifications.breakNotification(context, state)

        assertEquals(FocusNotificationTheme.ACCENT_BREAK, notification.color)
        assertEquals("Descanso em andamento", textOf(notification))
        assertTrue(!textOf(notification).contains("ciclo", ignoreCase = true))
        assertEquals(1, notification.actions.size)
        assertEquals("Pular descanso", notification.actions[0].title.toString())
        assertEquals(endTime, notification.`when`)
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER))
    }

    @Test
    fun completionNotification_dungeon_showsPurpleAccentAndGpReward() {
        val notification = FocusNotifications.completionNotification(
            context, xp = 60, gold = 75,
            isDungeonMode = true, isWildernessChecked = false
        )

        assertEquals(FocusNotificationTheme.ACCENT_DUNGEON, notification.color)
        assertEquals(R.drawable.ic_focus_dungeon, notification.smallIcon.resId)
        assertEquals("+60 XP · +75 GP", textOf(notification))
    }

    @Test
    fun completionNotification_standard_showsGoldAccent() {
        val notification = FocusNotifications.completionNotification(
            context, xp = 10, gold = 20,
            isDungeonMode = false, isWildernessChecked = false
        )

        assertEquals(FocusNotificationTheme.ACCENT_STANDARD, notification.color)
        assertEquals(R.drawable.ic_focus_small, notification.smallIcon.resId)
    }

    @Test
    fun cognitiveDeathNotification_streakLost_describesZeroedStreak() {
        val notification = FocusNotifications.cognitiveDeathNotification(
            context,
            CognitiveDeathResult(newStreak = 0, streakSaved = false, newCombo = 0),
            previousStreak = 10
        )

        val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()
        assertEquals("Morte Cognitiva", title)
        assertTrue(textOf(notification).contains("sequência foi zerada"))
        assertTrue(textOf(notification).contains("recompensa foi perdida"))
        assertTrue(notification.actions.isNullOrEmpty())
        assertNotNull(notification.contentIntent)
    }

    @Test
    fun cognitiveDeathNotification_streakSaved_describesPreservedStreak() {
        val notification = FocusNotifications.cognitiveDeathNotification(
            context,
            CognitiveDeathResult(newStreak = 10, streakSaved = true, newCombo = 0),
            previousStreak = 10
        )

        assertTrue(textOf(notification).contains("sequência foi preservada"))
    }

    @Test
    fun cognitiveDeathNotification_noPreviousStreak_describesAlreadyZeroed() {
        val notification = FocusNotifications.cognitiveDeathNotification(
            context,
            CognitiveDeathResult(newStreak = 0, streakSaved = false, newCombo = 0),
            previousStreak = 0
        )

        assertTrue(textOf(notification).contains("já estava zerada"))
    }

    @Test
    fun channels_includeEventsWithDefaultImportance() {
        FocusNotifications.createChannels(context)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val events = manager.getNotificationChannel(FocusNotifications.CHANNEL_EVENTS)

        assertNotNull(events)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, events.importance)
        assertEquals(1004, FocusNotifications.ID_COGNITIVE_DEATH)
        assertNull(events.id.takeIf { it == FocusNotifications.CHANNEL_SESSION })
    }
}
