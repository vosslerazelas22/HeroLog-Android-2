package com.iurispraecepta.herolog.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.iurispraecepta.herolog.HeroLogApplication
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.FocusUseCase
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Backup de conclusão via `AlarmManager` exato (spec-008, FR-9).
 *
 * Cobre o caso em que o próprio Foreground Service é morto pelo sistema: o alarme,
 * agendado no início de cada sessão/descanso, dispara este receiver independente do
 * processo do Service. Decisão 3 do plano: cobre **sessão e descanso**.
 *
 * O estado do backup vive em `SharedPreferences` (`focus_backup_prefs`), escrito pelo
 * Service a cada START/RESUME/START_BREAK e limpo nos términos normais — o receiver
 * nunca depende de memória do processo morto.
 */
class FocusSessionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_BACKUP = "com.iurispraecepta.herolog.service.ACTION_BACKUP"
        const val EXTRA_KIND = "extra_backup_kind"
        const val KIND_SESSION = "session"
        const val KIND_BREAK = "break"

        private const val PREFS = "focus_backup_prefs"
        private const val KEY_KIND = "kind"
        private const val KEY_END_MILLIS = "end_millis"
        private const val REQUEST_CODE = 9001

        fun scheduleSessionBackup(context: Context, endTimeMillis: Long) {
            writeBackup(context, KIND_SESSION, endTimeMillis)
            scheduleExact(context, endTimeMillis)
        }

        fun scheduleBreakBackup(context: Context, endTimeMillis: Long) {
            writeBackup(context, KIND_BREAK, endTimeMillis)
            scheduleExact(context, endTimeMillis)
        }

        fun cancelBackup(context: Context) {
            clearBackup(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(backupPendingIntent(context))
        }

        fun pendingBackup(context: Context): Pair<String?, Long> {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val kind = prefs.getString(KEY_KIND, null)
            val end = prefs.getLong(KEY_END_MILLIS, 0L)
            return kind to end
        }

        private fun writeBackup(context: Context, kind: String, endTimeMillis: Long) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_KIND, kind)
                .putLong(KEY_END_MILLIS, endTimeMillis)
                .apply()
        }

        private fun clearBackup(context: Context) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        }

        private fun backupIntent(context: Context): Intent =
            Intent(context, FocusSessionReceiver::class.java).apply { action = ACTION_BACKUP }

        private fun backupPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context, REQUEST_CODE, backupIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        private fun scheduleExact(context: Context, endTimeMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = backupPendingIntent(context)
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                runCatching { alarmManager.canScheduleExactAlarms() }.getOrDefault(false)
            } else {
                true
            }
            try {
                if (canExact) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, endTimeMillis, pendingIntent
                    )
                } else {
                    // Fallback inexato (FR-9 degradado, nunca silenciosamente perdido).
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, endTimeMillis, pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, endTimeMillis, pendingIntent
                )
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_BACKUP) return
        val goAsync = goAsync()
        scope.launch {
            try {
                handleBackup(context.applicationContext)
            } finally {
                goAsync.finish()
            }
        }
    }

    internal suspend fun handleBackup(context: Context) {
        val (kind, end) = pendingBackup(context)
        if (kind == null) return
        if (System.currentTimeMillis() < end) {
            // Alarme inexato (fallback) disparou cedo: reagenda para o fim real
            // em vez de perder a conclusão.
            scheduleExact(context, end)
            return
        }
        val app = context as? HeroLogApplication ?: return
        when (kind) {
            KIND_SESSION -> {
                val session = app.focusSessionRepository.getSession()
                if (!shouldHandleSessionBackup(session, System.currentTimeMillis())) {
                    // Service vivo tratou, ou já concluída: só limpa o registro.
                    clearBackup(context)
                    return
                }
                val completed = completeSessionFromBackup(
                    app.characterRepository,
                    app.focusSessionRepository,
                    app.pendingRewardRepository,
                    session!!.config,
                    session.durationMinutes
                )
                clearBackup(context)
                if (completed) {
                    val pending = app.pendingRewardRepository.getPending().lastOrNull()
                    if (pending != null) {
                        FocusNotifications.notifyIfAllowed(
                            context,
                            FocusNotifications.ID_COMPLETED,
                            FocusNotifications.completionNotification(
                                context, pending.xpGained, pending.goldGained
                            )
                        )
                    }
                }
            }
            KIND_BREAK -> {
                clearBackup(context)
                val focusMinutes = app.characterRepository.getCharacterState()
                    ?.pomodoroSettings?.focusDuration?.takeIf { it > 0 } ?: 25
                FocusNotifications.notifyIfAllowed(
                    context,
                    FocusNotifications.ID_SUGGESTION,
                    FocusNotifications.suggestionNotification(context, focusMinutes)
                )
            }
        }
    }
}

/**
 * Guarda de idempotência (testável): o backup só age quando existe sessão ativa
 * ainda não calculada e já expirada. `pendingCalculation != null` significa que o
 * Service (ou o ViewModel, com app aberto) já concluiu — não tocar.
 */
fun shouldHandleSessionBackup(session: PersistedFocusSession?, now: Long): Boolean {
    if (session == null) return false
    if (session.pendingCalculation != null) return false
    return now >= session.endTimeMillis
}

/**
 * Conclusão via backup: calcula, aplica com defaults de background, enfileira a
 * celebração e limpa a sessão ativa. Retorna `false` sem efeitos quando não há
 * personagem persistido.
 */
suspend fun completeSessionFromBackup(
    characterRepository: CharacterRepository,
    focusSessionRepository: FocusSessionRepository,
    pendingRewardRepository: PendingRewardRepository,
    config: FocusSessionConfig,
    durationMinutes: Int,
    referenceDate: Date = Date()
): Boolean {
    if (characterRepository.getCharacterState() == null) return false
    val useCase = FocusUseCase(characterRepository, focusSessionRepository, pendingRewardRepository)
    val calc = useCase.calculateRewards(config, durationMinutes) ?: return false
    useCase.applyRewards(calc, referenceDate = referenceDate) ?: return false
    focusSessionRepository.clearSession()
    return true
}
