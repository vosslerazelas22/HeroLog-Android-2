package com.iurispraecepta.herolog

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.service.FocusNotificationTheme
import com.iurispraecepta.herolog.service.FocusNotifications
import com.iurispraecepta.herolog.service.FocusSessionService
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.ServiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * T202/T203 — Confirmação de abandono na Masmorra (spec NOTIFICACOES_FOCO, FR-C5).
 * T204 parcial: o roteamento Padrão/Selvagem → direto e Masmorra → REQUEST é
 * decidido pela pura `abandonButtonAction`; o job de reversão de 5 s é validado
 * em device (scope do Service não aceita dispatcher de teste).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AbandonConfirmationNotificationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun intentActionOf(pendingIntent: PendingIntent): String? =
        shadowOf(pendingIntent).savedIntent?.action

    private fun textOf(notification: Notification): String =
        notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString()

    private fun titleOf(notification: Notification): String =
        notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()

    @Test
    fun timerNotification_dungeon_abandonPointsToRequest() {
        val state = ServiceState(
            phase = ServicePhase.RUNNING,
            sessionConfig = FocusSessionConfig(0, false, true, 2),
            skillName = "Kotlin",
            durationMinutes = 25,
            endTimeMillis = System.currentTimeMillis() + 60_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)
        val abandon = notification.actions.first { it.title.toString() == "Abandonar" }

        assertEquals(FocusSessionService.ACTION_ABANDON_REQUEST, intentActionOf(abandon.actionIntent))
    }

    @Test
    fun timerNotification_standard_abandonPointsDirectlyToAbandon() {
        val state = ServiceState(
            phase = ServicePhase.RUNNING,
            sessionConfig = FocusSessionConfig(0, false, false, 0),
            skillName = "Kotlin",
            durationMinutes = 25,
            endTimeMillis = System.currentTimeMillis() + 60_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)
        val abandon = notification.actions.first { it.title.toString() == "Abandonar" }

        assertEquals(FocusSessionService.ACTION_ABANDON, intentActionOf(abandon.actionIntent))
    }

    @Test
    fun timerNotification_armedConfirmation_showsConfirmActionsAndRedBackground() {
        val state = ServiceState(
            phase = ServicePhase.RUNNING,
            sessionConfig = FocusSessionConfig(0, false, true, 2),
            skillName = "Kotlin",
            durationMinutes = 25,
            endTimeMillis = System.currentTimeMillis() + 60_000L,
            abandonConfirmDeadlineMillis = System.currentTimeMillis() + 5_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals("Abandonar a Masmorra?", titleOf(notification))
        assertEquals("Você perderá o progresso da masmorra (2/4).", textOf(notification))
        assertEquals(2, notification.actions.size)
        assertEquals("Confirmar abandono", notification.actions[0].title.toString())
        assertEquals("Voltar", notification.actions[1].title.toString())
        assertEquals(
            FocusSessionService.ACTION_ABANDON,
            intentActionOf(notification.actions[0].actionIntent)
        )
        assertEquals(
            FocusSessionService.ACTION_ABANDON_CANCEL,
            intentActionOf(notification.actions[1].actionIntent)
        )
        assertEquals(FocusNotificationTheme.BG_ABANDON_CONFIRM, notification.color)
        // Em RUNNING o cronômetro nativo continua visível durante a confirmação.
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER))
    }

    @Test
    fun timerNotification_armedConfirmation_firstSession_showsExpeditionText() {
        val state = ServiceState(
            phase = ServicePhase.RUNNING,
            sessionConfig = FocusSessionConfig(0, false, true, 0),
            skillName = "Kotlin",
            durationMinutes = 25,
            endTimeMillis = System.currentTimeMillis() + 60_000L,
            abandonConfirmDeadlineMillis = System.currentTimeMillis() + 5_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals("Abandonar a Masmorra?", titleOf(notification))
        assertEquals("Você perderá esta expedição.", textOf(notification))
    }

    @Test
    fun timerNotification_armedConfirmation_progressSessions_showsCountText() {
        for (n in 1..3) {
            val state = ServiceState(
                phase = ServicePhase.RUNNING,
                sessionConfig = FocusSessionConfig(0, false, true, n),
                skillName = "Kotlin",
                durationMinutes = 25,
                endTimeMillis = System.currentTimeMillis() + 60_000L,
                abandonConfirmDeadlineMillis = System.currentTimeMillis() + 5_000L
            )

            val notification = FocusNotifications.timerNotification(context, state)

            assertEquals(
                "Você perderá o progresso da masmorra ($n/4).",
                textOf(notification)
            )
        }
    }

    @Test
    fun abandonButtonAction_routesDungeonToRequestAndOthersDirect() {
        assertEquals(
            FocusSessionService.ACTION_ABANDON_REQUEST,
            FocusNotifications.abandonButtonAction(isDungeonMode = true)
        )
        assertEquals(
            FocusSessionService.ACTION_ABANDON,
            FocusNotifications.abandonButtonAction(isDungeonMode = false)
        )
    }
}
