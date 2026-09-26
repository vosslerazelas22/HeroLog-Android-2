package com.iurispraecepta.herolog.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.iurispraecepta.herolog.HeroLogApplication
import com.iurispraecepta.herolog.MainActivity
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.FocusUseCase
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

/**
 * Foreground Service dono do timer de foco/descanso (spec-008, FR-1 a FR-8).
 *
 * Fonte única de verdade do estado em execução ([state]); o ViewModel apenas
 * observa (fio ligado no bloco T10). A recompensa é calculada e aplicada em
 * background via [FocusUseCase] com os defaults `editedNotes = ""` /
 * `selectedTag = null`.
 *
 * Exclusão mútua com o ViewModel (contrato bilateral): ao concluir, o Service
 * recarrega a sessão do Room — se `pendingCalculation != null`, o ViewModel
 * concluiu primeiro (app aberto) e o Service sai do caminho sem aplicar.
 * O lado ViewModel dessa guarda entra no bloco T10.
 */
class FocusSessionService : Service() {

    companion object {
        private const val TAG = "FocusSessionService"
        const val ACTION_START = "com.iurispraecepta.herolog.service.ACTION_START"
        const val ACTION_PAUSE = "com.iurispraecepta.herolog.service.ACTION_PAUSE"
        const val ACTION_RESUME = "com.iurispraecepta.herolog.service.ACTION_RESUME"
        const val ACTION_STOP = "com.iurispraecepta.herolog.service.ACTION_STOP"
        const val ACTION_START_BREAK =
            "com.iurispraecepta.herolog.service.ACTION_START_BREAK"
        const val ACTION_SKIP_BREAK = "com.iurispraecepta.herolog.service.ACTION_SKIP_BREAK"
        const val ACTION_START_NEW_SESSION =
            "com.iurispraecepta.herolog.service.ACTION_START_NEW_SESSION"

        const val EXTRA_CONFIG = "extra_config_json"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_BREAK_MINUTES = "extra_break_minutes"

        /**
         * Fallback de skill — idêntico ao de `FocusRewardsLogic.calculate`, para que
         * o título da notificação nunca divirja do da celebração.
         */
        const val FALLBACK_SKILL_NAME = "Código Sagrado"

        private val _state = MutableStateFlow(ServiceState())
        val state: StateFlow<ServiceState> = _state

        fun actionIntent(context: Context, action: String): android.app.PendingIntent =
            FocusNotifications.servicePendingIntent(
                context, action,
                requestCode = action.hashCode()
            )

        fun openAppIntent(context: Context): android.app.PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return android.app.PendingIntent.getActivity(
                context, 0, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }

        /** Chamado pela UI (bloco T10) ao iniciar uma sessão. */
        fun startSession(context: Context, config: FocusSessionConfig, durationMinutes: Int) {
            val intent = Intent(context, FocusSessionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CONFIG, JsonConfig.default.encodeToString(config))
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null

    @Volatile
    private var destroyed = false

    private fun repos(): HeroLogApplication = application as HeroLogApplication

    private fun useCase(): FocusUseCase {
        val app = repos()
        return FocusUseCase(
            app.characterRepository,
            app.focusSessionRepository,
            app.pendingRewardRepository
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        FocusNotifications.createChannels(this)
    }

    override fun onDestroy() {
        destroyed = true
        tickJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        // startForeground() síncrono: o sistema exige a chamada em ~5s após o start.
        // Nada assíncrono (Room, alarme) pode acontecer antes dela — nem mesmo no
        // caminho de intent inválido (stop sem foreground também dispara
        // ForegroundServiceDidNotStartInTimeException).
        if (!prestartForeground(intent, action)) {
            Log.e(TAG, "invalid start extras; stopping after provisional foreground")
            stopSelf()
            return START_NOT_STICKY
        }
        when (action) {
            ACTION_START -> {
                val (config, duration) = parseStartExtras(intent) ?: return START_NOT_STICKY
                handleStart(config, duration)
            }
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP -> handleStop()
            ACTION_START_BREAK -> {
                val minutes = intent.getIntExtra(EXTRA_BREAK_MINUTES, 0)
                handleStartBreak(minutes.takeIf { it > 0 })
            }
            ACTION_SKIP_BREAK -> handleSkipBreak()
            ACTION_START_NEW_SESSION -> handleStartNewSession()
        }
        return START_NOT_STICKY
    }

    private fun parseStartExtras(intent: Intent): Pair<FocusSessionConfig, Int>? {
        val configJson = intent.getStringExtra(EXTRA_CONFIG) ?: return null
        val duration = intent.getIntExtra(EXTRA_DURATION_MINUTES, 0)
        if (duration <= 0) return null
        val config = runCatching {
            JsonConfig.default.decodeFromString<FocusSessionConfig>(configJson)
        }.getOrNull() ?: return null
        return config to duration
    }

    /**
     * Posta a notificação de foreground de forma síncrona, antes de qualquer
     * trabalho assíncrono — SEMPRE posta, mesmo com intent inválido (o chamador
     * dá stopSelf em seguida; parar sem foreground também crasha).
     * Usa estado provisório (sem I/O); os handlers atualizam o conteúdo assim
     * que o Room responder. Retorna false se o intent for inválido.
     */
    private fun prestartForeground(intent: Intent, action: String): Boolean {
        val provisional = when (action) {
            ACTION_START -> {
                val parsed = parseStartExtras(intent)
                if (parsed == null) {
                    Log.e(TAG, "ACTION_START sem extras válidos")
                    return postProvisionalAndFail()
                }
                val (config, duration) = parsed
                FocusNotifications.timerNotification(
                    this,
                    ServiceState(
                        phase = ServicePhase.RUNNING,
                        sessionConfig = config,
                        skillName = FALLBACK_SKILL_NAME,
                        durationMinutes = duration,
                        endTimeMillis = System.currentTimeMillis() + duration * 60_000L
                    )
                )
            }
            ACTION_START_BREAK -> FocusNotifications.breakNotification(this, _state.value)
            else -> FocusNotifications.timerNotification(this, _state.value)
        }
        goForeground(FocusNotifications.ID_TIMER, provisional)
        return true
    }

    private fun postProvisionalAndFail(): Boolean {
        goForeground(
            FocusNotifications.ID_TIMER,
            FocusNotifications.timerNotification(this, ServiceState())
        )
        return false
    }

    // ---- Sessão ----

    private fun handleStart(config: FocusSessionConfig, durationMinutes: Int) {
        val current = _state.value
        if (current.phase == ServicePhase.RUNNING || current.phase == ServicePhase.PAUSED) return
        tickJob?.cancel()
        val now = System.currentTimeMillis()
        val endTime = now + durationMinutes * 60_000L
        scope.launch {
            if (destroyed) return@launch
            val charState = repos().characterRepository.getCharacterState()
            val skillName = charState?.skills?.getOrNull(config.selectedSkillIdx)?.name
                ?: FALLBACK_SKILL_NAME
            _state.value = ServiceState(
                phase = ServicePhase.RUNNING,
                sessionConfig = config,
                skillName = skillName,
                durationMinutes = durationMinutes,
                endTimeMillis = endTime,
                pauseCount = 0
            )
            // Persiste a sessão ativa SEM calcular: o cálculo (com Random) acontece
            // uma única vez, na conclusão — nunca no start/resume (anti-reroll).
            repos().focusSessionRepository.saveSession(
                PersistedFocusSession(
                    config = config,
                    durationMinutes = durationMinutes,
                    endTimeMillis = endTime,
                    pendingCalculation = null
                )
            )
            FocusSessionReceiver.scheduleSessionBackup(this@FocusSessionService, endTime)
            if (destroyed) return@launch
            goForeground(FocusNotifications.ID_TIMER, FocusNotifications.timerNotification(this@FocusSessionService, _state.value))
            startTick()
        }
    }

    private fun handlePause() {
        if (_state.value.phase != ServicePhase.RUNNING) return
        tickJob?.cancel()
        val remaining = _state.value.sessionRemainingMillis(System.currentTimeMillis())
        _state.value = _state.value.copy(
            phase = ServicePhase.PAUSED,
            pausedRemainingMillis = remaining,
            pauseCount = _state.value.pauseCount + 1
        )
        scope.launch {
            if (destroyed) return@launch
            // Espelha o ViewModel: sessão pausada não é recuperável entre reinícios.
            repos().focusSessionRepository.clearSession()
            FocusSessionReceiver.cancelBackup(this@FocusSessionService)
            if (destroyed) return@launch
            goForeground(FocusNotifications.ID_TIMER, FocusNotifications.timerNotification(this@FocusSessionService, _state.value))
        }
    }

    private fun handleResume() {
        val current = _state.value
        if (current.phase != ServicePhase.PAUSED || current.sessionConfig == null) return
        tickJob?.cancel()
        val endTime = System.currentTimeMillis() + current.pausedRemainingMillis
        _state.value = current.copy(
            phase = ServicePhase.RUNNING,
            endTimeMillis = endTime,
            pausedRemainingMillis = 0L
        )
        scope.launch {
            if (destroyed) return@launch
            repos().focusSessionRepository.saveSession(
                PersistedFocusSession(
                    config = current.sessionConfig,
                    durationMinutes = current.durationMinutes,
                    endTimeMillis = endTime,
                    pendingCalculation = null
                )
            )
            FocusSessionReceiver.scheduleSessionBackup(this@FocusSessionService, endTime)
            if (destroyed) return@launch
            goForeground(FocusNotifications.ID_TIMER, FocusNotifications.timerNotification(this@FocusSessionService, _state.value))
            startTick()
        }
    }

    private fun handleStop() {
        tickJob?.cancel()
        scope.launch {
            repos().focusSessionRepository.clearSession()
            FocusSessionReceiver.cancelBackup(this@FocusSessionService)
            FocusNotifications.cancel(this@FocusSessionService, FocusNotifications.ID_TIMER)
            FocusNotifications.cancel(this@FocusSessionService, FocusNotifications.ID_COMPLETED)
            FocusNotifications.cancel(this@FocusSessionService, FocusNotifications.ID_SUGGESTION)
            _state.value = ServiceState()
            stopForegroundAndSelf()
        }
    }

    private fun startTick() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive) {
                delay(1_000L)
                if (destroyed) return@launch
                val current = _state.value
                if (current.phase == ServicePhase.RUNNING) {
                    if (current.sessionRemainingMillis(System.currentTimeMillis()) <= 0L) {
                        completeSession()
                        return@launch
                    }
                    FocusNotifications.notifyIfAllowed(
                        this@FocusSessionService,
                        FocusNotifications.ID_TIMER,
                        FocusNotifications.timerNotification(this@FocusSessionService, current)
                    )
                } else if (current.phase == ServicePhase.BREAK_RUNNING) {
                    if (current.breakRemainingMillis(System.currentTimeMillis()) <= 0L) {
                        completeBreak()
                        return@launch
                    }
                    FocusNotifications.notifyIfAllowed(
                        this@FocusSessionService,
                        FocusNotifications.ID_TIMER,
                        FocusNotifications.breakNotification(this@FocusSessionService, current)
                    )
                } else {
                    return@launch
                }
            }
        }
    }

    private suspend fun completeSession() {
        if (destroyed) return
        tickJob?.cancel()
        val current = _state.value
        val config = current.sessionConfig
        if (config == null) {
            handleStop()
            return
        }
        // Guarda bilateral: só aplica detendo registro vivo não calculado.
        // ViewModel concluiu (pending != null) OU backup/dados externos já
        // resolveram (sessão ausente) → sai do caminho sem aplicar.
        // Cálculo único da sessão (start/resume persistem sem calcular),
        // então não há pendingCalculation salvo a reaproveitar aqui.
        val persisted = repos().focusSessionRepository.getSession()
        if (!shouldServiceCompleteSession(persisted)) {
            FocusSessionReceiver.cancelBackup(this)
            FocusNotifications.cancel(this, FocusNotifications.ID_TIMER)
            _state.value = ServiceState()
            stopForegroundAndSelf()
            return
        }
        val calc = useCase().calculateRewards(config, current.durationMinutes)
            ?: return handleStop()
        useCase().applyRewards(calc) ?: return handleStop()
        repos().focusSessionRepository.clearSession()
        FocusSessionReceiver.cancelBackup(this)
        FocusNotifications.cancel(this, FocusNotifications.ID_TIMER)
        if (destroyed) return
        val xp = calc.xpEarned
        val gold = calc.goldEarned + calc.dungeonClearGoldBonus
        _state.value = current.copy(
            phase = ServicePhase.COMPLETED,
            lastCompletedXp = xp,
            lastCompletedGold = gold,
            lastSessionDungeonMode = config.isDungeonMode,
            lastSessionWildernessMode = config.isWildernessChecked,
            lastSessionDungeonSessions = config.dungeonSessions
        )
        goForeground(
            FocusNotifications.ID_COMPLETED,
            FocusNotifications.completionNotification(this, xp, gold)
        )
    }

    // ---- Descanso / nova sessão (FR-6/FR-7/FR-8) ----

    private fun handleStartBreak(requestedMinutes: Int?) {
        val current = _state.value
        if (current.phase != ServicePhase.COMPLETED) return
        tickJob?.cancel()
        scope.launch {
            if (destroyed) return@launch
            val minutes = requestedMinutes ?: defaultBreakMinutes(current)
            val now = System.currentTimeMillis()
            val endTime = now + minutes * 60_000L
            _state.value = current.copy(
                phase = ServicePhase.BREAK_RUNNING,
                breakEndTimeMillis = endTime,
                breakDurationMinutes = minutes,
                breakTotalSeconds = minutes * 60
            )
            FocusSessionReceiver.scheduleBreakBackup(this@FocusSessionService, endTime)
            FocusNotifications.cancel(this@FocusSessionService, FocusNotifications.ID_COMPLETED)
            if (destroyed) return@launch
            goForeground(FocusNotifications.ID_TIMER, FocusNotifications.breakNotification(this@FocusSessionService, _state.value))
            startTick()
        }
    }

    private suspend fun defaultBreakMinutes(current: ServiceState): Int {
        val charState = repos().characterRepository.getCharacterState()
        val settings = charState?.pomodoroSettings
        val dungeonCleared =
            current.lastSessionDungeonMode && current.lastSessionDungeonSessions + 1 >= 4
        return if (dungeonCleared) {
            settings?.longBreakDuration?.takeIf { it > 0 } ?: 15
        } else {
            settings?.shortBreakDuration?.takeIf { it > 0 } ?: 5
        }
    }

    private suspend fun completeBreak() {
        if (destroyed) return
        tickJob?.cancel()
        FocusSessionReceiver.cancelBackup(this)
        FocusNotifications.cancel(this, FocusNotifications.ID_TIMER)
        val focusMinutes = repos().characterRepository.getCharacterState()
            ?.pomodoroSettings?.focusDuration?.takeIf { it > 0 }
            ?: _state.value.durationMinutes.takeIf { it > 0 } ?: 25
        _state.value = _state.value.copy(phase = ServicePhase.SUGGESTING)
        if (destroyed) return
        goForeground(
            FocusNotifications.ID_SUGGESTION,
            FocusNotifications.suggestionNotification(this, focusMinutes)
        )
    }

    private fun handleSkipBreak() {
        val phase = _state.value.phase
        if (phase != ServicePhase.COMPLETED && phase != ServicePhase.BREAK_RUNNING) return
        tickJob?.cancel()
        scope.launch {
            if (destroyed) return@launch
            FocusSessionReceiver.cancelBackup(this@FocusSessionService)
            FocusNotifications.cancel(this@FocusSessionService, FocusNotifications.ID_TIMER)
            FocusNotifications.cancel(this@FocusSessionService, FocusNotifications.ID_COMPLETED)
            completeBreak()
        }
    }

    /**
     * Reusa a config original da sessão concluída (skill, modos, dungeonSessions).
     * O Service detém a cadeia sessão → descanso → nova sessão com o app fechado;
     * o ViewModel sincroniza o progresso (ex.: dungeonSessionsProgress) ao reabrir
     * o app (bloco T10) — divergência transitória consciente, não bug.
     */
    private fun handleStartNewSession() {
        val current = _state.value
        val config = current.sessionConfig ?: return
        if (current.phase != ServicePhase.SUGGESTING) return
        tickJob?.cancel()
        scope.launch {
            if (destroyed) return@launch
            FocusNotifications.cancel(this@FocusSessionService, FocusNotifications.ID_SUGGESTION)
            val focusMinutes = repos().characterRepository.getCharacterState()
                ?.pomodoroSettings?.focusDuration?.takeIf { it > 0 }
                ?: current.durationMinutes.takeIf { it > 0 } ?: 25
            handleStart(config, focusMinutes)
        }
    }

    // ---- Foreground ----

    private fun goForeground(id: Int, notification: android.app.Notification) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, id, notification, type)
    }

    private fun stopForegroundAndSelf() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
