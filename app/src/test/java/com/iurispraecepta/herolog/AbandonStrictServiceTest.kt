package com.iurispraecepta.herolog

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.service.AbandonEvent
import com.iurispraecepta.herolog.service.FocusSessionService
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.ServiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * TAREFA 2 (TAREFA 2 da revisão, T204 da tasks) — `handleAbandon` estrito na
 * Masmorra (Q3) e limpeza do prazo, no Service real via `ServiceController`
 * (mesmo padrão de `FocusServiceStartTest` / `FocusServiceSelfCancelTest`).
 *
 * Sobre RED/GREEN: o comportamento já estava implementado (bloco anterior);
 * estes testes são de caracterização — entram direto GREEN. O RED do TDD foi
 * exercido na camada pura/notificação (funções inexistentes antes).
 *
 * Relógio: o prazo usa wall clock real (`now < deadline`); sem injeção de
 * relógio, os testes constroem deadlines relativos ao `now` real (futuro =
 * armado, passado = expirado). Nenhum teste espera os 5 s reais.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AbandonStrictServiceTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val dungeonConfig = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = true,
        dungeonSessions = 2
    )
    private val standardConfig = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = false,
        dungeonSessions = 0
    )
    private val wildernessConfig = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = true,
        isDungeonMode = false,
        dungeonSessions = 0
    )

    private val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val events = mutableListOf<AbandonEvent>()

    private fun app(): HeroLogApplication =
        context.applicationContext as HeroLogApplication

    private fun sendAction(service: FocusSessionService, action: String) {
        val intent = Intent(context, FocusSessionService::class.java).apply {
            this.action = action
        }
        service.onStartCommand(intent, 0, 0)
    }

    private fun runningDungeon(deadline: Long): ServiceState = ServiceState(
        phase = ServicePhase.RUNNING,
        sessionConfig = dungeonConfig,
        skillName = "Kotlin",
        durationMinutes = 25,
        endTimeMillis = System.currentTimeMillis() + 60_000L,
        abandonConfirmDeadlineMillis = deadline
    )

    @Before
    fun setUp() {
        FocusSessionService.setStateForTests(ServiceState())
        events.clear()
        testScope.launch {
            FocusSessionService.abandonEvents.collect { events.add(it) }
        }
        runBlocking {
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
        testScope.cancel()
        FocusSessionService.setStateForTests(ServiceState())
        runBlocking {
            app().focusSessionRepository.clearSession()
        }
    }

    private fun pollIdle(timeoutMs: Long = 10_000L): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (FocusSessionService.state.value.phase == ServicePhase.IDLE) return true
            Thread.sleep(100L)
        }
        return false
    }

    @Test
    fun dungeon_abandonWithoutRequest_isIgnored_sessionContinues() {
        FocusSessionService.setStateForTests(runningDungeon(deadline = 0L))
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        sendAction(controller.get(), FocusSessionService.ACTION_ABANDON)

        Thread.sleep(800L)

        assertTrue("nenhum AbandonEvent deveria ser emitido", events.isEmpty())
        assertEquals(ServicePhase.RUNNING, FocusSessionService.state.value.phase)

        controller.destroy()
    }

    @Test
    fun dungeon_requestThenAbandonInsideWindow_emitsEventAndStops() {
        FocusSessionService.setStateForTests(runningDungeon(deadline = 0L))
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        val service = controller.get()
        sendAction(service, FocusSessionService.ACTION_ABANDON_REQUEST)

        assertTrue(
            "REQUEST deveria armar a confirmação",
            FocusSessionService.state.value.abandonConfirmDeadlineMillis > 0L
        )
        sendAction(service, FocusSessionService.ACTION_ABANDON)

        assertTrue("ABANDON dentro do prazo deveria encerrar", pollIdle())
        assertEquals(1, events.size)
        assertTrue(events[0].isDungeonMode)

        controller.destroy()
    }

    @Test
    fun dungeon_requestThenCancel_clearsDeadline_laterAbandonIgnored() {
        FocusSessionService.setStateForTests(runningDungeon(deadline = 0L))
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        val service = controller.get()
        sendAction(service, FocusSessionService.ACTION_ABANDON_REQUEST)
        sendAction(service, FocusSessionService.ACTION_ABANDON_CANCEL)

        assertEquals(0L, FocusSessionService.state.value.abandonConfirmDeadlineMillis)
        assertEquals(ServicePhase.RUNNING, FocusSessionService.state.value.phase)

        sendAction(service, FocusSessionService.ACTION_ABANDON)
        Thread.sleep(800L)

        assertTrue("ABANDON após CANCEL deveria ser ignorado", events.isEmpty())
        assertEquals(ServicePhase.RUNNING, FocusSessionService.state.value.phase)

        controller.destroy()
    }

    @Test
    fun dungeon_secondRequestWhileArmed_isIdempotent_keepsDeadline() {
        FocusSessionService.setStateForTests(runningDungeon(deadline = 0L))
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        val service = controller.get()
        sendAction(service, FocusSessionService.ACTION_ABANDON_REQUEST)
        val first = FocusSessionService.state.value.abandonConfirmDeadlineMillis

        Thread.sleep(150L)
        sendAction(service, FocusSessionService.ACTION_ABANDON_REQUEST)

        assertEquals(first, FocusSessionService.state.value.abandonConfirmDeadlineMillis)

        controller.destroy()
    }

    @Test
    fun dungeon_expiredDeadline_abandonIsIgnored() {
        FocusSessionService.setStateForTests(
            runningDungeon(deadline = System.currentTimeMillis() - 1_000L)
        )
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        sendAction(controller.get(), FocusSessionService.ACTION_ABANDON)

        Thread.sleep(800L)

        assertTrue(events.isEmpty())
        assertEquals(ServicePhase.RUNNING, FocusSessionService.state.value.phase)

        controller.destroy()
    }

    @Test
    fun standard_abandon_abandonsInOneTouch() {
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.RUNNING,
                sessionConfig = standardConfig,
                skillName = "Kotlin",
                durationMinutes = 25,
                endTimeMillis = System.currentTimeMillis() + 60_000L
            )
        )
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        sendAction(controller.get(), FocusSessionService.ACTION_ABANDON)

        assertTrue(pollIdle())
        assertEquals(1, events.size)

        controller.destroy()
    }

    @Test
    fun wilderness_abandon_abandonsInOneTouch() {
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.RUNNING,
                sessionConfig = wildernessConfig,
                skillName = "Kotlin",
                durationMinutes = 25,
                endTimeMillis = System.currentTimeMillis() + 60_000L
            )
        )
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        sendAction(controller.get(), FocusSessionService.ACTION_ABANDON)

        assertTrue(pollIdle())
        assertEquals(1, events.size)

        controller.destroy()
    }

    @Test
    fun stopWhileArmed_clearsDeadline() {
        FocusSessionService.setStateForTests(runningDungeon(deadline = 0L))
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        val service = controller.get()
        sendAction(service, FocusSessionService.ACTION_ABANDON_REQUEST)
        assertTrue(FocusSessionService.state.value.abandonConfirmDeadlineMillis > 0L)

        sendAction(service, FocusSessionService.ACTION_STOP)

        assertTrue(pollIdle())
        assertEquals(0L, FocusSessionService.state.value.abandonConfirmDeadlineMillis)

        controller.destroy()
    }

    @Test
    fun startBreakWhileArmed_clearsDeadline() {
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.COMPLETED,
                abandonConfirmDeadlineMillis = System.currentTimeMillis() + 60_000L
            )
        )
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START_BREAK
            putExtra(FocusSessionService.EXTRA_BREAK_MINUTES, 5)
        }
        controller.get().onStartCommand(intent, 0, 0)

        val deadline = System.currentTimeMillis() + 10_000L
        var broke = false
        while (System.currentTimeMillis() < deadline) {
            if (FocusSessionService.state.value.phase == ServicePhase.BREAK_RUNNING) {
                broke = true
                break
            }
            Thread.sleep(100L)
        }

        assertTrue("descanso deveria iniciar", broke)
        assertEquals(0L, FocusSessionService.state.value.abandonConfirmDeadlineMillis)

        controller.destroy()
    }

    @Test
    fun sessionCompletionWhileArmed_clearsDeadline() {
        val now = System.currentTimeMillis()
        runBlocking {
            app().focusSessionRepository.saveSession(
                com.iurispraecepta.herolog.logic.focus.PersistedFocusSession(
                    config = dungeonConfig,
                    durationMinutes = 25,
                    endTimeMillis = now - 1_000L,
                    pendingCalculation = null
                )
            )
        }
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.PAUSED,
                sessionConfig = dungeonConfig,
                skillName = "Kotlin",
                durationMinutes = 25,
                pausedRemainingMillis = 0L,
                abandonConfirmDeadlineMillis = now + 60_000L
            )
        )
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        sendAction(controller.get(), FocusSessionService.ACTION_RESUME)

        val deadline = System.currentTimeMillis() + 15_000L
        var completed = false
        while (System.currentTimeMillis() < deadline) {
            if (FocusSessionService.state.value.phase == ServicePhase.COMPLETED) {
                completed = true
                break
            }
            Thread.sleep(250L)
        }

        assertTrue("sessão deveria completar pelo tick", completed)
        assertEquals(0L, FocusSessionService.state.value.abandonConfirmDeadlineMillis)

        controller.destroy()
    }

    @Test
    fun requestOutsideRunningOrPaused_doesNotArm() {        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.IDLE,
                sessionConfig = dungeonConfig
            )
        )
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        sendAction(controller.get(), FocusSessionService.ACTION_ABANDON_REQUEST)

        Thread.sleep(300L)

        assertEquals(0L, FocusSessionService.state.value.abandonConfirmDeadlineMillis)
        assertTrue(events.isEmpty())

        controller.destroy()
    }

    /**
     * Revisão CodeRabbit (1): sem consumir o prazo, o segundo ACTION_ABANDON
     * passa no guarda (o `handleStop` do primeiro ainda não zerou o `_state`,
     * pois é assíncrono) e emite um 2º evento → consequência duplicada no VM.
     */
    @Test
    fun dungeon_doubleAbandonInsideWindow_emitsExactlyOneEvent() {
        FocusSessionService.setStateForTests(runningDungeon(deadline = 0L))
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        val service = controller.get()
        sendAction(service, FocusSessionService.ACTION_ABANDON_REQUEST)
        assertTrue(FocusSessionService.state.value.abandonConfirmDeadlineMillis > 0L)

        sendAction(service, FocusSessionService.ACTION_ABANDON)
        sendAction(service, FocusSessionService.ACTION_ABANDON)

        assertTrue(pollIdle())
        assertEquals(
            "dois ABANDON na janela deveriam consumir o prazo no primeiro",
            1, events.size
        )

        controller.destroy()
    }

    /**
     * Revisão CodeRabbit (2): ACTION_ABANDON enquanto a conclusão está em
     * andamento deve ser ignorado (E1 — a conclusão prevalece).
     *
     * Determinístico via histórico: um coletor `Dispatchers.Unconfined` grava
     * todo `ServiceState` desde antes do RESUME; a asserção é que existe um
     * estado com phase RUNNING/PAUSED e prazo zerado ANTES do primeiro
     * COMPLETED. Com o fix (prazo limpo na 1ª linha de `completeSession`)
     * esse estado existe; sem o fix o prazo só zera junto de COMPLETED
     * (L532-541), então o RED é determinístico (demonstrado revertendo o
     * fix). Substitui a versão anterior por poll de 100 ms, que perdia a
     * janela transitória quando a máquina estava rápida (4/5 falhas).
     * Não depende de `handleResume` limpar o prazo: ele preserva (linhas
     * 370-374), e é disso que o teste precisa.
     */
    @Test
    fun dungeon_abandonDuringCompletion_isIgnored() {
        val now = System.currentTimeMillis()
        runBlocking {
            app().focusSessionRepository.saveSession(
                com.iurispraecepta.herolog.logic.focus.PersistedFocusSession(
                    config = dungeonConfig,
                    durationMinutes = 25,
                    endTimeMillis = now - 1_000L,
                    pendingCalculation = null
                )
            )
        }
        FocusSessionService.setStateForTests(
            ServiceState(
                phase = ServicePhase.PAUSED,
                sessionConfig = dungeonConfig,
                skillName = "Kotlin",
                durationMinutes = 25,
                pausedRemainingMillis = 0L,
                abandonConfirmDeadlineMillis = now + 60_000L
            )
        )
        val history = java.util.concurrent.CopyOnWriteArrayList<ServiceState>()
        val historyJob = testScope.launch(Dispatchers.Unconfined) {
            FocusSessionService.state.collect { history.add(it) }
        }
        val controller = Robolectric.buildService(FocusSessionService::class.java).create()
        sendAction(controller.get(), FocusSessionService.ACTION_RESUME)

        val endDeadline = System.currentTimeMillis() + 15_000L
        var completed = false
        while (System.currentTimeMillis() < endDeadline) {
            if (FocusSessionService.state.value.phase == ServicePhase.COMPLETED) {
                completed = true
                break
            }
            Thread.sleep(100L)
        }
        assertTrue("sessão deveria completar", completed)

        // Dá um respiro para o coletor processar a última emissão.
        Thread.sleep(300L)
        historyJob.cancel()

        val firstCompleted = history.indexOfFirst { it.phase == ServicePhase.COMPLETED }
        assertTrue("histórico deveria conter COMPLETED", firstCompleted >= 0)
        val entryCleared = history.subList(0, firstCompleted).any {
            (it.phase == ServicePhase.RUNNING || it.phase == ServicePhase.PAUSED) &&
                it.abandonConfirmDeadlineMillis == 0L
        }
        assertTrue(
            "conclusão deveria consumir o prazo na entrada (ainda RUNNING/PAUSED)",
            entryCleared
        )

        // Complemento: após concluída, ABANDON é ignorado (prazo zerado).
        sendAction(controller.get(), FocusSessionService.ACTION_ABANDON)
        Thread.sleep(800L)
        assertTrue(
            "ABANDON após a conclusão deveria ser ignorado",
            events.isEmpty()
        )
        assertEquals(ServicePhase.COMPLETED, FocusSessionService.state.value.phase)

        controller.destroy()
    }
}
