package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.iurispraecepta.herolog.ui.theme.Cinzel

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
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .border(1.dp, Color.White.copy(alpha = 0.1f)),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 1. Modo
        QuickActionButton(
            label = "Modo",
            enabled = !isRunning,
            onClick = onOpenModeModal,
            modifier = Modifier.weight(1f)
        ) {
            when {
                isDungeonMode -> Icon(painterResource(R.drawable.lucide_ic_shield), null, tint = Color(0xFFC084FC))
                isWildernessMode -> Icon(painterResource(R.drawable.lucide_ic_shield_alert), null, tint = Color(0xFFF87171))
                else -> Icon(painterResource(R.drawable.lucide_ic_sparkles), null, tint = Color(0xFFE5C158))
            }
        }
        // 2. Som Ambiente
        QuickActionButton(
            label = "Som",
            enabled = true,
            onClick = onOpenAmbientModal,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activeAmbientIcon != null) {
                    Text(activeAmbientIcon, fontSize = 18.sp)
                    // Dot verde pulsante indicando trilha ativa
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399))
                    )
                } else {
                    Icon(painterResource(R.drawable.lucide_ic_volume_x), null, tint = Color(0xFFA1A1AA))
                }
            }
        }
        // 3. Ajustes
        QuickActionButton(
            label = "Ajustes",
            enabled = isSettingsEnabled,
            onClick = onOpenSettingsModal,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_ic_settings),
                contentDescription = null,
                tint = if (isSettingsEnabled) Color(0xFFA1A1AA) else Color(0xFF52525B)
            )
        }
        // 4. Tela Cheia
        QuickActionButton(
            label = "Tela Cheia",
            enabled = true,
            onClick = onEnterFullscreen,
            modifier = Modifier.weight(1f)
        ) {
            Icon(painterResource(R.drawable.lucide_ic_maximize), null, tint = Color(0xFFA1A1AA))
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor = when {
        isPressed -> Color(0xFF1C1917).copy(alpha = 0.4f) // stone-900/40
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = ripple()
            )
            .padding(vertical = 8.dp)
            .height(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontFamily = Cinzel,
                fontSize = 9.sp,
                letterSpacing = 0.05.em,
                color = if (enabled) Color(0xFFA8A29E) else Color(0xFF52525B)
            )
        )
    }
}
