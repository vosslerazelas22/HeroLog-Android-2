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

    const val ID_TIMER = 1001
    const val ID_COMPLETED = 1002
    const val ID_SUGGESTION = 1003

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
    }

    /** Sem permissão visível não há notificação — mas a recompensa continua aplicada (FR-14). */
    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun timerNotification(context: Context, state: ServiceState): Notification {
        val paused = state.phase == ServicePhase.PAUSED
        val remaining = formatRemaining(state.sessionRemainingMillis(System.currentTimeMillis()))
        val toggleAction = if (paused) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play, context.getString(R.string.focus_action_resume),
                FocusSessionService.actionIntent(context, FocusSessionService.ACTION_RESUME)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause, context.getString(R.string.focus_action_pause),
                FocusSessionService.actionIntent(context, FocusSessionService.ACTION_PAUSE)
            )
        }
        return NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(R.drawable.ic_focus_small)
            .setContentTitle(context.getString(R.string.focus_timer_title, state.skillName))
            .setContentText(
                if (paused) {
                    context.getString(R.string.focus_timer_text_paused, remaining)
                } else {
                    context.getString(R.string.focus_timer_text, remaining)
                }
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(FocusSessionService.openAppIntent(context))
            .addAction(toggleAction)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.focus_action_stop),
                FocusSessionService.actionIntent(context, FocusSessionService.ACTION_STOP)
            )
            .build()
    }

    fun breakNotification(context: Context, state: ServiceState): Notification {
        val remaining = formatRemaining(state.breakRemainingMillis(System.currentTimeMillis()))
        return NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(R.drawable.ic_focus_small)
            .setContentTitle(context.getString(R.string.focus_break_title))
            .setContentText(context.getString(R.string.focus_break_text, remaining))
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

    fun completionNotification(context: Context, xp: Int, gold: Int): Notification {
        return NotificationCompat.Builder(context, CHANNEL_REWARDS)
            .setSmallIcon(R.drawable.ic_focus_small)
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

    fun notifyIfAllowed(context: Context, id: Int, notification: Notification) {
        if (!canPost(context)) return
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
