package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.focus.formatDungeonCooldown
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.ui.theme.Amber300
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Amber950
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Emerald400
import com.iurispraecepta.herolog.ui.theme.Purple200
import com.iurispraecepta.herolog.ui.theme.Purple300
import com.iurispraecepta.herolog.ui.theme.Purple400
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Purple950
import com.iurispraecepta.herolog.ui.theme.Red300
import com.iurispraecepta.herolog.ui.theme.Red400
import com.iurispraecepta.herolog.ui.theme.Red500
import com.iurispraecepta.herolog.ui.theme.Red950
import com.iurispraecepta.herolog.ui.theme.Stone300
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.ui.theme.Stone900

@Composable
fun IncursionModeModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    currentMode: RaidMode,
    dungeonCooldownRemainingMs: Long = 0L,
    onSelectMode: (RaidMode) -> Unit,
) {
    val isDungeonOnCooldown = dungeonCooldownRemainingMs > 0L

    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = "⚔️ Modo de Incursão",
        variant = ModalVariant.Amber
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Selecione o estilo de jornada para sua próxima sessão de foco:",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp,
                    color = Stone300.copy(alpha = 0.6f),
                    lineHeight = 16.sp
                )
            )

            // Card 1: Padrão
            IncursionModeCard(
                title = "🎯 Padrão",
                description = "Chance de saque baseada na duração da sessão.",
                iconRes = LucideR.drawable.lucide_ic_sparkles,
                iconTint = Champagne400,
                isActive = currentMode == RaidMode.PADRAO,
                activeBadgeText = "ATIVO",
                activeBadgeBg = Champagne500.copy(alpha = 0.2f),
                activeBadgeTextColor = Champagne300,
                activeBadgeBorderColor = Champagne500.copy(alpha = 0.4f),
                activeBorderColor = Champagne400,
                baseBorderColor = Amber500.copy(alpha = 0.20f),
                baseBgColor = Amber950.copy(alpha = 0.15f),
                titleColor = Champagne300,
                descriptionColor = Color(0xB3D6D3D1),
                enabled = true,
                onClick = {
                    onSelectMode(RaidMode.PADRAO)
                    onClose()
                }
            )

            // Card 2: Masmorra
            IncursionModeCard(
                title = "⚔️ Masmorra",
                description = "4 sessões seguidas sem abandonar. +2.500 GP e Quad Loot ao concluir.",
                iconRes = LucideR.drawable.lucide_ic_swords,
                iconTint = Purple400,
                isActive = currentMode == RaidMode.MASMORRA && !isDungeonOnCooldown,
                activeBadgeText = if (isDungeonOnCooldown) "⏳ ${formatDungeonCooldown(dungeonCooldownRemainingMs)}" else "ATIVO",
                activeBadgeBg = if (isDungeonOnCooldown) Purple950 else Purple500.copy(alpha = 0.2f),
                activeBadgeTextColor = if (isDungeonOnCooldown) Purple200 else Purple300,
                activeBadgeBorderColor = if (isDungeonOnCooldown) Purple500.copy(alpha = 0.40f) else null,
                alwaysShowBadge = isDungeonOnCooldown,
                activeBorderColor = Purple400,
                baseBorderColor = Purple500.copy(alpha = 0.20f),
                baseBgColor = Purple950.copy(alpha = 0.25f),
                titleColor = Purple200,
                descriptionColor = Color(0xB3E9D5FF),
                enabled = !isDungeonOnCooldown,
                onClick = {
                    onSelectMode(RaidMode.MASMORRA)
                    onClose()
                }
            )

            // Card 3: Selvagem
            IncursionModeCard(
                title = "💀 Selvagem",
                description = "+25% XP & GP. Minimizar a aba cancela o bônus.",
                iconRes = LucideR.drawable.lucide_ic_skull,
                iconTint = Red400,
                isActive = currentMode == RaidMode.SELVAGEM,
                activeBadgeText = "ATIVO",
                activeBadgeBg = Red500.copy(alpha = 0.2f),
                activeBadgeTextColor = Red300,
                activeBorderColor = Red500,
                activeBadgeBorderColor = Red500.copy(alpha = 0.4f),
                baseBorderColor = Red500.copy(alpha = 0.25f),
                baseBgColor = Red950.copy(alpha = 0.25f),
                titleColor = Color(0xFFFECACA),
                descriptionColor = Color(0xB3FCA5A5),
                enabled = true,
                onClick = {
                    onSelectMode(RaidMode.SELVAGEM)
                    onClose()
                }
            )
        }
    }
}

@Composable
private fun IncursionModeCard(
    title: String,
    description: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    iconTint: Color,
    isActive: Boolean,
    activeBadgeText: String,
    activeBadgeBg: Color,
    activeBadgeTextColor: Color,
    activeBorderColor: Color,
    baseBorderColor: Color,
    baseBgColor: Color,
    titleColor: Color,
    descriptionColor: Color,
    enabled: Boolean,
    activeBadgeBorderColor: Color? = null,
    alwaysShowBadge: Boolean = false,
    onClick: () -> Unit,
) {
    val borderColor = if (isActive) activeBorderColor else baseBorderColor
    val showBadge = isActive || alwaysShowBadge

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Stone950)
            .background(baseBgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .then(if (!enabled) Modifier.alpha(0.55f) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Stone900)
                            .border(1.dp, baseBorderColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = titleColor,
                            letterSpacing = 0.8.sp
                        )
                    )
                }

                if (showBadge) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(activeBadgeBg)
                            .then(
                                if (activeBadgeBorderColor != null) {
                                    Modifier.border(1.dp, activeBadgeBorderColor, RoundedCornerShape(4.dp))
                                } else Modifier
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activeBadgeText,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = activeBadgeTextColor,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }

            Text(
                text = description,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = descriptionColor
                )
            )
        }
    }
}
