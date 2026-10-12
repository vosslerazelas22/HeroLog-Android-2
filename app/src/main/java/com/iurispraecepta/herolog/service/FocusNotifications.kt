package com.iurispraecepta.herolog.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.iurispraecepta.herolog.R
import com.iurispraecepta.herolog.logic.focus.CognitiveDeathResult
import com.iurispraecepta.herolog.ui.focus.RaidMode

/**
 * Canais, IDs e builders de notificação do Foco (spec-008, FR-1/FR-2/FR-6/FR-8/FR-14/FR-15).
 *
 * Dois canais separados por importância: o timer (`focus_session`, LOW, silencioso)
 * permanece ativo durante toda a sessão e nunca deve soar a cada atualização;
 * eventos pontuais (conclusão, sugestão) usam `focus_rewards` (DEFAULT, com som).
 * IDs fixos reaproveitados — nunca empilha duplicadas do mesmo tipo (FR-15).
 */
object FocusNotifications {

    const val CHANNEL_SESSION = "focus_session"
    const val CHANNEL_REWARDS = "focus_rewards"
    const val CHANNEL_EVENTS = "focus_events"

    const val ID_TIMER = 1001
    const val ID_COMPLETED = 1002
    const val ID_SUGGESTION = 1003
    const val ID_COGNITIVE_DEATH = 1004

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SESSION,
                context.getString(R.string.focus_channel_session),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.focus_channel_session_desc)
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REWARDS,
                context.getString(R.string.focus_channel_rewards),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.focus_channel_rewards_desc)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_EVENTS,
                context.getString(R.string.focus_channel_events),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.focus_channel_events_desc)
            }
        )
    }

    /** Sem permissão visível não há notificação — mas a recompensa continua aplicada (FR-14). */
    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Notificação de sessão (spec notificações por modo, FR-1 a FR-6, FR-10;
     * spec NOTIFICACOES_FOCO, FR-C1 a FR-C5).
     *
     * Fundo colorido por modo/estado via [FocusNotificationTheme.backgroundFor]
     * (`setColor` + `setColorized(true)`); título `Foco: <emoji> <nome>`;
     * pausado sem hora do relógio e tempo `MM:SS`/`H:MM:SS`. Wilderness em
     * andamento é mínima e sem ações; pausada (DEATH-PROOF) só Retomar.
     * O botão de encerrar é "Abandonar" — na Masmorra ele arma a confirmação
     * em dois toques (`ACTION_ABANDON_REQUEST`), nos demais modos abandona
     * direto (`ACTION_ABANDON`); `ACTION_STOP` segue sem consequência.
     */
    /**
     * Roteamento do botão "Abandonar" (puro, FR-C5): na Masmorra ele arma a
     * confirmação (`ACTION_ABANDON_REQUEST`); nos demais modos abandona direto.
     */
    fun abandonButtonAction(isDungeonMode: Boolean): String =
        if (isDungeonMode) FocusSessionService.ACTION_ABANDON_REQUEST
        else FocusSessionService.ACTION_ABANDON

    /**
     * Nome de exibição do título (puro, FR-C3): `"<emoji> <nome>"`, ou só o
     * nome quando o emoji vier nulo/branco (Q4, sem espaço extra).
     */
    fun displayNameFor(skillName: String, skillEmoji: String): String =
        if (skillEmoji.isBlank()) skillName else "$skillEmoji $skillName"

    fun timerNotification(context: Context, state: ServiceState): Notification {
        val config = state.sessionConfig
        val theme = FocusNotificationTheme.forSession(
            isDungeonMode = config?.isDungeonMode == true,
            isWildernessChecked = config?.isWildernessChecked == true
        )
        val now = System.currentTimeMillis()
        val paused = state.phase == ServicePhase.PAUSED
        val isWilderness = theme.mode == RaidMode.SELVAGEM
        val isDungeon = theme.mode == RaidMode.MASMORRA
        // Confirmação só existe na Masmorra (FR-C5); armada = prazo vigente.
        val confirming = isDungeon && state.isAbandonArmed(now)
        val background = FocusNotificationTheme.backgroundFor(theme.mode, state.phase, confirming)

        val title = if (confirming) context.getString(R.string.focus_abandon_confirm_title)
        else context.getString(
            R.string.focus_timer_title,
            displayNameFor(state.skillName, state.skillEmoji)
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(theme.smallIconRes)
            .setColor(background)
            .setColorized(true)
            .setContentTitle(title)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(FocusSessionService.openAppIntent(context))

        when {
            confirming -> {
                val sessions = config?.dungeonSessions ?: 0
                val text = if (sessions > 0) context.getString(
                    R.string.focus_abandon_confirm_text, sessions
                ) else context.getString(R.string.focus_abandon_confirm_text_first)
                builder.setContentText(text)
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        context.getString(R.string.focus_action_confirm_abandon),
                        FocusSessionService.actionIntent(context, FocusSessionService.ACTION_ABANDON)
                    )
                    .addAction(
                        android.R.drawable.ic_media_play, context.getString(R.string.focus_action_back),
                        FocusSessionService.actionIntent(context, FocusSessionService.ACTION_ABANDON_CANCEL)
                    )
                if (!paused) {
                    // Em RUNNING o cronômetro nativo continua visível (FR-C5.3).
                    builder.withCountDownChronometer(state.endTimeMillis)
                } else {
                    // Em PAUSED o texto de confirmação substitui o tempo congelado.
                    builder.setUsesChronometer(false).setShowWhen(false)
                }
            }
            isWilderness && !paused -> {
                builder.setContentText(context.getString(R.string.focus_timer_wilderness_running))
                    .withCountDownChronometer(state.endTimeMillis)
            }
            paused -> {
                val frozen = formatRemaining(state.sessionRemainingMillis(now))
                builder.setContentText(context.getString(R.string.focus_timer_text_paused, frozen))
                    .setUsesChronometer(false)
                    // Pausado sem hora do relógio no cabeçalho (FR-C4).
                    .setShowWhen(false)
                    .addAction(
                        android.R.drawable.ic_media_play, context.getString(R.string.focus_action_resume),
                        FocusSessionService.actionIntent(context, FocusSessionService.ACTION_RESUME)
                    )
                if (!isWilderness) {
                    builder.addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        context.getString(R.string.focus_action_abandon),
                        FocusSessionService.actionIntent(context, abandonButtonAction(isDungeon))
                    )
                }
            }
            else -> {
                val support = when (theme.mode) {
                    RaidMode.MASMORRA -> context.getString(
                        R.string.focus_timer_support_dungeon,
                        // Mesma regra da tela de foco (`ModeStatus`: pips com a
                        // contagem ANTES da sessão atual; aqui em texto "n/4").
                        config?.dungeonSessions ?: 0
                    )
                    else -> context.getString(
                        R.string.focus_timer_support_standard,
                        state.durationMinutes
                    )
                }
                builder.setContentText(support)
                    .withCountDownChronometer(state.endTimeMillis)
                    .addAction(
                        android.R.drawable.ic_media_pause, context.getString(R.string.focus_action_pause),
                        FocusSessionService.actionIntent(context, FocusSessionService.ACTION_PAUSE)
                    )
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        context.getString(R.string.focus_action_abandon),
                        FocusSessionService.actionIntent(context, abandonButtonAction(isDungeon))
                    )
            }
        }
        return builder.build()
    }

    /**
     * Descanso (FR-7): acento esmeralda em qualquer modo, contagem pelo
     * cronômetro nativo, ação "Pular descanso", sem texto de ciclo.
     */
    fun breakNotification(context: Context, state: ServiceState): Notification {
        return NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(R.drawable.ic_focus_small)
            .setColor(FocusNotificationTheme.BG_BREAK)
            .setColorized(true)
            .setContentTitle(context.getString(R.string.focus_break_title))
            .setContentText(context.getString(R.string.focus_break_text_running))
            .withCountDownChronometer(state.breakEndTimeMillis)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(FocusSessionService.openAppIntent(context))
            .addAction(
                android.R.drawable.ic_media_play, context.getString(R.string.focus_action_skip_break),
                FocusSessionService.actionIntent(context, FocusSessionService.ACTION_SKIP_BREAK)
            )
            .build()
    }

    /**
     * Conclusão (FR-8): acento e ícone do modo da sessão concluída,
     * recompensa como "+X XP · +Y GP".
     */
    fun completionNotification(
        context: Context,
        xp: Int,
        gold: Int,
        isDungeonMode: Boolean,
        isWildernessChecked: Boolean
    ): Notification {
        val theme = FocusNotificationTheme.forSession(isDungeonMode, isWildernessChecked)
        return NotificationCompat.Builder(context, CHANNEL_REWARDS)
            .setSmallIcon(theme.smallIconRes)
            .setColor(theme.accentColor)
            .setContentTitle(context.getString(R.string.focus_completed_title))
            .setContentText(context.getString(R.string.focus_completed_text, xp, gold))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(FocusSessionService.openAppIntent(context))
            .setDeleteIntent(
                FocusSessionService.actionIntent(context, FocusSessionService.ACTION_STOP)
            )
            .addAction(
                android.R.drawable.ic_media_play, context.getString(R.string.focus_action_start_break),
                FocusSessionService.actionIntent(context, FocusSessionService.ACTION_START_BREAK)
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.focus_action_skip_break),
                FocusSessionService.actionIntent(context, FocusSessionService.ACTION_SKIP_BREAK)
            )
            .build()
    }

    fun suggestionNotification(context: Context, focusMinutes: Int): Notification {
        return NotificationCompat.Builder(context, CHANNEL_REWARDS)
            .setSmallIcon(R.drawable.ic_focus_small)
            .setContentTitle(context.getString(R.string.focus_suggestion_title))
            .setContentText(context.getString(R.string.focus_suggestion_text, focusMinutes))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(FocusSessionService.openAppIntent(context))
            .setDeleteIntent(
                FocusSessionService.actionIntent(context, FocusSessionService.ACTION_STOP)
            )
            .addAction(
                android.R.drawable.ic_media_play, context.getString(R.string.focus_action_start_session),
                FocusSessionService.actionIntent(context, FocusSessionService.ACTION_START_NEW_SESSION)
            )
            .build()
    }

    /**
     * Morte Cognitiva pós-fato (FR-9): informa que o usuário saiu do app durante
     * o Wilderness, que a recompensa foi perdida e o que aconteceu com a
     * sequência (zerada, salva pelo Ranger ou já zerada). Sem ação de reviver —
     * o toque abre o app, onde o `CognitiveDeathOverlay` conduz o respawn
     * (que tem custo e não deve ser de um toque).
     */
    fun cognitiveDeathNotification(
        context: Context,
        result: CognitiveDeathResult,
        previousStreak: Int
    ): Notification {
        val textRes = when {
            result.streakSaved -> R.string.focus_cognitive_death_text_streak_saved
            previousStreak > 0 -> R.string.focus_cognitive_death_text_streak_lost
            else -> R.string.focus_cognitive_death_text_no_streak
        }
        return NotificationCompat.Builder(context, CHANNEL_EVENTS)
            .setSmallIcon(R.drawable.ic_focus_wilderness)
            .setColor(FocusNotificationTheme.ACCENT_WILDERNESS)
            .setContentTitle(context.getString(R.string.focus_cognitive_death_title))
            .setContentText(context.getString(textRes))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(textRes)))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(FocusSessionService.openAppIntent(context))
            .build()
    }

    fun notifyIfAllowed(context: Context, id: Int, notification: Notification) {        if (!canPost(context)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }

    fun cancel(context: Context, id: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(id)
    }

    internal fun servicePendingIntent(
        context: Context,
        action: String,
        requestCode: Int,
        extras: Intent.() -> Unit = {}
    ): PendingIntent {
        val intent = Intent(context, FocusSessionService::class.java).apply {
            this.action = action
            extras()
        }
        // getForegroundService: o tap numa ação com app em background ganha isenção p/ FGS.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
