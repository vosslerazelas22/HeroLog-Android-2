package com.iurispraecepta.herolog

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.service.FocusSessionService
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.ServiceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regressão do bug de auto-cancelamento do tick (spec-008): `startTick()` chamava
 * `completeSession()` inline na coroutine do próprio `tickJob`, e a primeira linha
 * de `completeSession()` (`tickJob?.cancel()`) cancelava a si mesma. O próximo
 * `suspend` (Room `getSession()`) lançava `CancellationException` silenciosa: a
 * sessão travava em RUNNING/"00:00" pra sempre, sem recompensa e sem COMPLETED.
 *
 * O teste sobe o Service real e usa o override `EXTRA_END_TIME_MILLIS` (~2.5s) para
 * forçar a conclusão pelo caminho ao vivo do tick — um teste que chamasse
 * `completeSession()` isolada (fora da coroutine do tick) NÃO reproduziria o bug.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusServiceSelfCancelTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val config = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = false,
        dungeonSessions = 0
    )

    private fun app(): HeroLogApplication =
        context.applicationContext as HeroLogApplication

    @Before
    fun setUp() {
        FocusSessionService.setStateForTests(ServiceState())
        runBlocking {
            // completeSession retorna via handleStop (IDLE) sem personagem; o cenário
            // real sempre tem um — semeia para exercer calcular → aplicar → COMPLETED.
            if (app().characterRepository.getCharacterState() == null) {
                app().characterRepository.saveCharacterState(
                    createInitialCharacterState()
                )
            }
            app().focusSessionRepository.clearSession()
        }
    }

    @After
    fun tearDown() {
        FocusSessionService.setStateForTests(ServiceState())
        runBlocking {
            app().focusSessionRepository.clearSession()
        }
    }

    @Test
    fun tickReachingZero_completesSession_appliesRewardsAndCelebrates() {
        val endTime = System.currentTimeMillis() + 2_500L
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
            putExtra(
                FocusSessionService.EXTRA_CONFIG,
                JsonConfig.default.encodeToString(config)
            )
            putExtra(FocusSessionService.EXTRA_DURATION_MINUTES, 25)
            putExtra(FocusSessionService.EXTRA_END_TIME_MILLIS, endTime)
        }
        val controller = Robolectric.buildService(FocusSessionService::class.java, intent).create()
        controller.startCommand(0, 0)

        // Caminho ao vivo: tick de 1s em Dispatchers.Default, conclusão ~3-4s.
        // Com o bug, a phase fica presa em RUNNING até o timeout (self-cancel).
        var completed = false
        runBlocking {
            val deadline = System.currentTimeMillis() + 15_000L
            while (System.currentTimeMillis() < deadline) {
                if (FocusSessionService.state.value.phase == ServicePhase.COMPLETED) {
                    completed = true
                    break
                }
                delay(250L)
            }
        }

        assertTrue(
            "tick zerou mas a sessão não completou (phase=${FocusSessionService.state.value.phase}) — self-cancel do tickJob?",
            completed
        )
        assertEquals(ServicePhase.COMPLETED, FocusSessionService.state.value.phase)
        assertTrue(
            FocusSessionService.state.value.lastCompletedXp > 0
        )

        runBlocking {
            assertNull(
                "sessão ativa deveria ter sido limpa na conclusão",
                app().focusSessionRepository.getSession()
            )
            assertTrue(
                "recompensa deveria ter sido enfileirada na conclusão",
                app().pendingRewardRepository.getPending().isNotEmpty()
            )
        }

        controller.destroy()
    }
}
