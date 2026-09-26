package com.iurispraecepta.herolog

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.service.FocusNotifications
import com.iurispraecepta.herolog.service.FocusSessionService
import kotlinx.serialization.encodeToString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Bloqueante #1: `startForeground()` precisa acontecer de forma síncrona no
 * `onStartCommand`, antes de qualquer I/O. Estes testes falhariam se o
 * `goForeground` voltasse para dentro da coroutine (nenhuma notificação
 * estaria postada antes de idle/wait).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusServiceStartTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val config = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = false,
        dungeonSessions = 0
    )

    private fun startIntent(durationMinutes: Int = 25): Intent =
        Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
            putExtra(
                FocusSessionService.EXTRA_CONFIG,
                JsonConfig.default.encodeToString(config)
            )
            putExtra(FocusSessionService.EXTRA_DURATION_MINUTES, durationMinutes)
        }

    private fun activeIds(): List<Int> {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager.activeNotifications.map { it.id }
    }

    @After
    fun tearDown() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    @Test
    fun startCommand_postsTimerNotificationSynchronously() {
        val controller = Robolectric.buildService(FocusSessionService::class.java, startIntent()).create()

        controller.startCommand(0, 0)

        // Sem idle/wait: só passa se o foreground foi postado no onStartCommand.
        assertTrue(activeIds().contains(FocusNotifications.ID_TIMER))
        controller.destroy()
    }

    @Test
    fun startCommand_withInvalidExtras_postsProvisionalButPersistsNothing() {
        val invalid = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
        }
        val controller = Robolectric.buildService(FocusSessionService::class.java, invalid).create()

        controller.startCommand(0, 0)

        // Foreground provisório evita ForegroundServiceDidNotStartInTimeException,
        // mas nenhuma sessão é persistida nem tick iniciado.
        assertTrue(activeIds().contains(FocusNotifications.ID_TIMER))
        val app = context.applicationContext as com.iurispraecepta.herolog.HeroLogApplication
        kotlinx.coroutines.runBlocking {
            assertNull(app.focusSessionRepository.getSession())
        }
        controller.destroy()
    }

    @Test
    fun startCommand_persistsActiveSessionWithoutCalculation() {
        val controller = Robolectric.buildService(FocusSessionService::class.java, startIntent()).create()
        controller.startCommand(0, 0)

        // A coroutine de persistência roda em Dispatchers.Default: espera ativa.
        val app = context.applicationContext as com.iurispraecepta.herolog.HeroLogApplication
        var session: com.iurispraecepta.herolog.logic.focus.PersistedFocusSession? = null
        val deadline = System.currentTimeMillis() + 10_000L
        // Anti-reroll: start/resume nunca calculam — pendingCalculation fica nulo.
        kotlinx.coroutines.runBlocking {
            while (System.currentTimeMillis() < deadline) {
                session = app.focusSessionRepository.getSession()
                if (session != null) break
                kotlinx.coroutines.delay(100L)
            }
        }

        assertNotNull(session)
        assertNull(session!!.pendingCalculation)
        assertEquals(25, session!!.durationMinutes)
        controller.destroy()
    }
}
