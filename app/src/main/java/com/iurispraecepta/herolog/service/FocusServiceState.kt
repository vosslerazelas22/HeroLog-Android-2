package com.iurispraecepta.herolog.service

import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig

/**
 * Estado único da sessão/descanso em execução (spec-008, FR-13).
 *
 * O `FocusSessionService` é a única fonte de verdade enquanto detém uma sessão;
 * o ViewModel apenas observa (bloco T10). Fora do Service, o estado é IDLE.
 */
enum class ServicePhase {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    BREAK_RUNNING,
    SUGGESTING
}

data class ServiceState(
    val phase: ServicePhase = ServicePhase.IDLE,
    val sessionConfig: FocusSessionConfig? = null,
    val skillName: String = "Foco",
    /** Emoji da skill para o título `Foco: <emoji> <nome>` (FR-C3). Vazio = sem emoji. */
    val skillEmoji: String = "",
    val durationMinutes: Int = 0,
    val endTimeMillis: Long = 0L,
    /** Restante congelado no momento da pausa (ms). Só válido em PAUSED. */
    val pausedRemainingMillis: Long = 0L,
    val pauseCount: Int = 0,
    val lastCompletedXp: Int = 0,
    val lastCompletedGold: Int = 0,
    val breakEndTimeMillis: Long = 0L,
    val breakDurationMinutes: Int = 0,
    val breakTotalSeconds: Int = 0,
    val lastSessionDungeonMode: Boolean = false,
    val lastSessionWildernessMode: Boolean = false,
    val lastSessionDungeonSessions: Int = 0,
    /**
     * Prazo da confirmação de abandono em Masmorra (ms, wall clock; FR-C5).
     * `0L` = sem confirmação armada. A decisão é por relógio (`now < deadline`,
     * NFR-C3), nunca por flag — o job de reversão só restaura a aparência.
     */
    val abandonConfirmDeadlineMillis: Long = 0L
) {
    /** Confirmação armada se o prazo ainda vigora. */
    fun isAbandonArmed(now: Long): Boolean =
        abandonConfirmDeadlineMillis > 0L && now < abandonConfirmDeadlineMillis
}

/** Restante da sessão em ms (nunca negativo). Em PAUSED retorna o valor congelado. */
fun ServiceState.sessionRemainingMillis(now: Long): Long =
    if (phase == ServicePhase.PAUSED) pausedRemainingMillis.coerceAtLeast(0L)
    else (endTimeMillis - now).coerceAtLeast(0L)

/** Restante do descanso em ms (nunca negativo). */
fun ServiceState.breakRemainingMillis(now: Long): Long =
    (breakEndTimeMillis - now).coerceAtLeast(0L)

fun formatRemaining(millis: Long): String {
    val totalSeconds = (millis / 1000L).toInt().coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    // Sessões de 60 min ou mais (Q2: preset 90 + custom até 180) usam H:MM:SS,
    // mesmo padrão do cronômetro nativo; abaixo disso, MM:SS com zero à esquerda.
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

/**
 * Guarda de conclusão do Service (achado em device 26/09): o Service só aplica
 * quando detém um registro vivo de sessão ainda não calculada.
 * - `null` (receiver completou e limpou, dados apagados, pausa em voo): NÃO
 *   aplicar — parar e sair do caminho. Sem isso, o tick com estado obsoleto
 *   recalculava e aplicava em duplicidade após o backup.
 * - `pendingCalculation != null` (ViewModel concluiu primeiro): NÃO aplicar.
 */
fun shouldServiceCompleteSession(
    session: com.iurispraecepta.herolog.logic.focus.PersistedFocusSession?
): Boolean = session != null && session.pendingCalculation == null
