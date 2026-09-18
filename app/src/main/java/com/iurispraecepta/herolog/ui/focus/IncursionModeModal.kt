package com.iurispraecepta.herolog.ui.focus

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.logic.focus.formatDungeonCooldown
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Purple200
import com.iurispraecepta.herolog.ui.theme.Purple300
import com.iurispraecepta.herolog.ui.theme.Purple400
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Purple950
import com.iurispraecepta.herolog.ui.theme.Red300
import com.iurispraecepta.herolog.ui.theme.Red400
import com.iurispraecepta.herolog.ui.theme.Red500
import com.iurispraecepta.herolog.ui.theme.Red950
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

// Fonte: HeroLog-React-ref/src/modules/focus/IncursionModeModal.tsx
// zinc-300 Tailwind = #d4d4d8. NOTA (18/09): o token Zinc300 de Color.kt foi
// corrigido para #D4D4D8 em trabalho paralelo (TimerSettingsModal, ainda não
// commitado) — mesmo valor deste literal, que é mantido de propósito para
// este commit não depender de arquivo alheio não commitado. Cleanup futuro
// pode migrar os 2 usos para o token sem mudar 1 pixel.

// purple-100 Tailwind = #f3e8ff (descrição card Masmorra, .tsx:105)
// red-100 Tailwind = #fee2e2 (descrição card Selvagem, .tsx:136)
private val Zinc300React = Color(0xFFD4D4D8)
private val Purple100React = Color(0xFFF3E8FF)
private val Red100React = Color(0xFFFEE2E2)

@Composable
fun IncursionModeModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    currentMode: RaidMode,
    dungeonCooldownRemainingMs: Long = 0L,
    onSelectMode: (RaidMode) -> Unit,
) {
    val isDungeonOnCooldown = dungeonCooldownRemainingMs > 0L
    val isPadraoActive = currentMode == RaidMode.PADRAO
    val isMasmorraActive = currentMode == RaidMode.MASMORRA && !isDungeonOnCooldown
    val isSelvagemActive = currentMode == RaidMode.SELVAGEM

    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = "⚔️ Modo de Incursão",
        variant = ModalVariant.Amber
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                // .tsx:34-35 — sentence-case literal; a caixa alta visual vem da
                // Cinzel (fonte unicase), sem .uppercase() manual (D1).
                text = "Selecione o estilo de jornada para sua próxima sessão de foco:",
                style = TextStyle(
                    fontFamily = Cinzel,
                    fontSize = 12.sp,
                    color = Zinc300React.copy(alpha = 0.6f),
                    lineHeight = 19.5.sp
                )
            )

            // Card 1: Padrão (.tsx:45-49)
            IncursionModeCard(
                title = "🎯 Padrão",
                description = "Chance de saque baseada na duração da sessão.",
                iconRes = LucideR.drawable.lucide_ic_sparkles,
                iconTint = Champagne400,
                isActive = isPadraoActive,
                // "ATIVO" hardcoded: o React tem string "Ativo" + classe CSS
                // `uppercase` (.tsx:59) e a badge usa JetBrainsMono (tem
                // minúsculas de verdade, ao contrário da Cinzel) — sem
                // TextTransform nesta toolchain, a string já vai em caps.
                activeBadgeText = "ATIVO",
                activeBadgeBg = Champagne500.copy(alpha = 0.2f),
                activeBadgeTextColor = Champagne300,
                activeBadgeBorderColor = Champagne500.copy(alpha = 0.4f),
                borderColor = if (isPadraoActive) {
                    Champagne400.copy(alpha = 0.8f)
                } else {
                    Color.White.copy(alpha = 0.10f)
                },
                bgColor = if (isPadraoActive) {
                    Champagne500.copy(alpha = 0.15f)
                } else {
                    Stone900.copy(alpha = 0.4f)
                },
                titleColor = Champagne300,
                descriptionColor = Zinc300React.copy(alpha = 0.7f),
                enabled = true,
                onClick = {
                    onSelectMode(RaidMode.PADRAO)
                    onClose()
                }
            )

            // Card 2: Masmorra (.tsx:79-85) — 3 estados: cooldown / ativo / inativo
            IncursionModeCard(
                title = "⚔️ Masmorra",
                description = "4 sessões seguidas sem abandonar. +2.500 GP e Quad Loot ao concluir.",
                iconRes = LucideR.drawable.lucide_ic_swords,
                iconTint = Purple400,
                isActive = isMasmorraActive,
                activeBadgeText = if (isDungeonOnCooldown) {
                    "⏳ ${formatDungeonCooldown(dungeonCooldownRemainingMs)}"
                } else {
                    // Mesmo regime do card 1: `uppercase` real no React (.tsx:100).
                    "ATIVO"
                },
                activeBadgeBg = if (isDungeonOnCooldown) {
                    Purple950.copy(alpha = 0.8f)
                } else {
                    Purple500.copy(alpha = 0.2f)
                },
                activeBadgeTextColor = Purple300,
                activeBadgeBorderColor = if (isDungeonOnCooldown) {
                    Purple500.copy(alpha = 0.30f)
                } else {
                    Purple500.copy(alpha = 0.4f)
                },
                badgeFontWeight = if (isDungeonOnCooldown) FontWeight.Medium else FontWeight.Bold,
                isCooldownBadge = isDungeonOnCooldown,
                alwaysShowBadge = isDungeonOnCooldown,
                borderColor = when {
                    isDungeonOnCooldown -> Purple500.copy(alpha = 0.15f)
                    isMasmorraActive -> Purple400.copy(alpha = 0.8f)
                    else -> Purple500.copy(alpha = 0.25f)
                },
                bgColor = when {
                    isDungeonOnCooldown -> Stone950.copy(alpha = 0.4f)
                    isMasmorraActive -> Purple950.copy(alpha = 0.8f)
                    else -> Stone900.copy(alpha = 0.4f)
                },
                titleColor = Purple200,
                descriptionColor = Purple100React.copy(alpha = 0.7f),
                enabled = !isDungeonOnCooldown,
                onClick = {
                    onSelectMode(RaidMode.MASMORRA)
                    onClose()
                }
            )

            // Card 3: Selvagem (.tsx:117-121)
            IncursionModeCard(
                title = "💀 Selvagem",
                description = "+25% XP & GP. Minimizar a aba cancela o bônus.",
                iconRes = LucideR.drawable.lucide_ic_skull,
                iconTint = Red400,
                isActive = isSelvagemActive,
                // `uppercase` real no React (.tsx:131) — ver nota no card 1.
                activeBadgeText = "ATIVO",
                activeBadgeBg = Red500.copy(alpha = 0.2f),
                activeBadgeTextColor = Red300,
                activeBadgeBorderColor = Red500.copy(alpha = 0.4f),
                borderColor = if (isSelvagemActive) {
                    Red500.copy(alpha = 0.8f)
                } else {
                    Red500.copy(alpha = 0.25f)
                },
                bgColor = if (isSelvagemActive) {
                    Red950.copy(alpha = 0.8f)
                } else {
                    Stone900.copy(alpha = 0.4f)
                },
                titleColor = Color(0xFFFECACA),
                descriptionColor = Red100React.copy(alpha = 0.7f),
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
    @DrawableRes iconRes: Int,
    iconTint: Color,
    isActive: Boolean,
    activeBadgeText: String,
    activeBadgeBg: Color,
    activeBadgeTextColor: Color,
    borderColor: Color,
    bgColor: Color,
    titleColor: Color,
    descriptionColor: Color,
    enabled: Boolean,
    activeBadgeBorderColor: Color? = null,
    badgeFontWeight: FontWeight = FontWeight.Bold,
    isCooldownBadge: Boolean = false,
    alwaysShowBadge: Boolean = false,
    onClick: () -> Unit,
) {
    val showBadge = isActive || alwaysShowBadge
    val cardShape = RoundedCornerShape(8.dp)

    // R1 — `transition-all` do React (.tsx:45,79,117; default Tailwind 150ms,
    // cubic-bezier(0.4,0,0.2,1) = FastOutSlowInEasing): bg, borda e opacity
    // do botão animam na troca de estado em vez de trocar instantâneo.
    // (Sombra do estado ativo não animada — micro-gap aceito.)
    val transitionSpec = tween<Color>(150, easing = FastOutSlowInEasing)
    val animatedBgColor by animateColorAsState(bgColor, transitionSpec, label = "incursionCardBg")
    val animatedBorderColor by animateColorAsState(borderColor, transitionSpec, label = "incursionCardBorder")
    val animatedAlpha by animateFloatAsState(
        if (!enabled) 0.5f else 1f,
        tween<Float>(150, easing = FastOutSlowInEasing),
        label = "incursionCardAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // shadow-md do estado ativo (.tsx:47,83,119). O ring-1 colorido do
            // React não tem equivalente direto no Compose — a borda ativa em
            // 80% de alpha + esta sombra são a aproximação consciente.
            .then(if (isActive) Modifier.shadow(6.dp, cardShape) else Modifier)
            .clip(cardShape)
            .background(animatedBgColor)
            .border(1.dp, animatedBorderColor, cardShape)
            .alpha(animatedAlpha)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    // Ícone solto inline com o título (.tsx:53,89,125) — sem
                    // wrapper Box; w-4 h-4 = 16dp (D7, D13).
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = titleColor
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
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        if (isCooldownBadge) {
                            // .tsx:95-98 — font-medium, tracking-normal, Clock
                            // w-3 h-3 (12dp) + gap-1 (4dp).
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = LucideR.drawable.lucide_ic_clock),
                                    contentDescription = null,
                                    tint = Purple400,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = activeBadgeText,
                                    style = TextStyle(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = badgeFontWeight,
                                        fontSize = 9.sp,
                                        color = activeBadgeTextColor
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = activeBadgeText,
                                style = TextStyle(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = badgeFontWeight,
                                    fontSize = 9.sp,
                                    color = activeBadgeTextColor,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }

            Text(
                text = description,
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    lineHeight = 19.5.sp,
                    color = descriptionColor
                )
            )
        }
    }
}
