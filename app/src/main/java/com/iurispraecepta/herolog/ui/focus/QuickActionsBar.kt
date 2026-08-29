package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R

@Composable
fun QuickActionsBar(
    isDungeonMode: Boolean,
    isWildernessMode: Boolean,
    isRunning: Boolean,
    onOpenModeModal: () -> Unit,
    activeAmbientIcon: String?, // emoji da trilha ativa, ou null
    onOpenAmbientModal: () -> Unit,
    isSettingsEnabled: Boolean, // fonte: disabled quando isRunning || isBreakActive
    onOpenSettingsModal: () -> Unit,
    onEnterFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 1. Modo
        QuickActionButton(
            label = "Modo",
            enabled = !isRunning,
            onClick = onOpenModeModal
        ) {
            when {
                isDungeonMode -> Icon(painterResource(R.drawable.lucide_ic_shield), null, tint = Color(0xFFC084FC))
                isWildernessMode -> Icon(painterResource(R.drawable.lucide_ic_shield_alert), null, tint = Color(0xFFF87171))
                else -> Icon(painterResource(R.drawable.lucide_ic_sparkles), null, tint = Color(0xFFE5C158))
            }
        }
        // 2. Som Ambiente
        QuickActionButton(label = "Som", enabled = true, onClick = onOpenAmbientModal) {
            if (activeAmbientIcon != null) {
                Text(activeAmbientIcon, fontSize = 18.sp)
            } else {
                Icon(painterResource(R.drawable.lucide_ic_volume_x), null, tint = Color(0xFFA1A1AA))
            }
        }
        // 3. Ajustes
        QuickActionButton(label = "Ajustes", enabled = isSettingsEnabled, onClick = onOpenSettingsModal) {
            Icon(
                painter = painterResource(R.drawable.lucide_ic_settings),
                contentDescription = null,
                tint = if (isSettingsEnabled) Color(0xFFA1A1AA) else Color(0xFF52525B)
            )
        }
        // 4. Tela Cheia
        QuickActionButton(label = "Tela Cheia", enabled = true, onClick = onEnterFullscreen) {
            Icon(painterResource(R.drawable.lucide_ic_maximize), null, tint = Color(0xFFA1A1AA))
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp)
            .width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
            label.uppercase(),
            fontSize = 9.sp,
            color = if (enabled) Color(0xFFA8A29E) else Color(0xFF52525B)
        )
    }
}
