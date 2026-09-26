package com.iurispraecepta.herolog.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Permissão `POST_NOTIFICATIONS` em runtime (spec-008, FR-14, Android 13+).
 *
 * A ausência da permissão nunca bloqueia a recompensa (FR-5): o Service aplica
 * via [FocusUseCase] e apenas omite as notificações visíveis.
 * O registro do launcher na `MainActivity` entra no bloco T10, junto do trigger.
 */
object FocusNotificationPermission {

    fun isGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldRequest(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isGranted(context)
    }

    fun registerLauncher(
        activity: ComponentActivity,
        onResult: (granted: Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            onResult(granted)
        }
    }
}
