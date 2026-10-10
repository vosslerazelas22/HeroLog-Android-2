package com.iurispraecepta.herolog

import android.app.Notification
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.service.FocusNotifications
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.ServiceState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * T104 — Título com emoji (spec NOTIFICACOES_FOCO, FR-C3).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotificationTitleTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun titleOf(notification: Notification): String =
        notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()

    @Test
    fun timerNotification_withEmoji_showsEmojiBeforeName() {
        val state = ServiceState(
            phase = ServicePhase.RUNNING,
            sessionConfig = FocusSessionConfig(0, false, false, 0),
            skillName = "Leitura",
            skillEmoji = "📚",
            durationMinutes = 25,
            endTimeMillis = System.currentTimeMillis() + 60_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals("Foco: 📚 Leitura", titleOf(notification))
    }

    @Test
    fun timerNotification_emptyEmoji_showsNameWithoutExtraSpace() {
        val state = ServiceState(
            phase = ServicePhase.RUNNING,
            sessionConfig = FocusSessionConfig(0, false, false, 0),
            skillName = "Leitura",
            skillEmoji = "",
            durationMinutes = 25,
            endTimeMillis = System.currentTimeMillis() + 60_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals("Foco: Leitura", titleOf(notification))
    }

    @Test
    fun timerNotification_blankEmoji_showsNameWithoutExtraSpace() {
        val state = ServiceState(
            phase = ServicePhase.RUNNING,
            sessionConfig = FocusSessionConfig(0, false, false, 0),
            skillName = "Leitura",
            skillEmoji = "   ",
            durationMinutes = 25,
            endTimeMillis = System.currentTimeMillis() + 60_000L
        )

        val notification = FocusNotifications.timerNotification(context, state)

        assertEquals("Foco: Leitura", titleOf(notification))
    }
}
