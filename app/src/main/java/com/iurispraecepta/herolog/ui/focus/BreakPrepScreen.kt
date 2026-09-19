package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Stone100
import com.iurispraecepta.herolog.ui.theme.Stone500
import com.iurispraecepta.herolog.ui.theme.Stone800

@Composable
fun BreakPrepScreen(
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    selectedBreakMinutes: Int,
    isDungeonMode: Boolean,
    onSelectDuration: (Int) -> Unit,
    onStartBreak: () -> Unit,
    onSkipBreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emeraldPrimary = Color(0xFF10B981)
    val emeraldLight = Color(0xFF34D399)
    val emeraldDark = Color(0xFF059669)
    val tealDark = Color(0xFF0D9488)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // App.tsx:2414 — my-3 (12px)
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                // Fonte: App.tsx:2414 — border border-emerald-500/35
                // bg-stone-950/70 rounded-lg (8px). shadow-inner do React
                // sem equivalente no Compose — gap consciente.
                .border(1.dp, emeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .testTag("break_prep_card"),
            color = Color(0xB30C0A09), // stone-950/70 (0xB3 = 70%)
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Fonte: App.tsx:2416 — overlay absolute inset-0
                    // bg-gradient-to-b from-emerald-500/5 to-transparent
                    // (desenhado atrás do conteúdo).
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                emeraldPrimary.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 32.dp), // py-8 px-5
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "MISSÃO CONCLUÍDA.",
                    fontFamily = Cinzel,
                    // Fonte: App.tsx:2419 — font-serif font-black text-[10px]
                    // tracking-[0.25em] text-emerald-500. Cinzel vai só até
                    // ExtraBold (800), sem Black (900) — divergência mínima
                    // documentada (Type.kt:35-40).
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    color = emeraldPrimary,
                    letterSpacing = 2.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Escolha a duração da pausa.",
                    fontFamily = Cinzel,
                    // Fonte: App.tsx:2420 — text-xs (12px) text-stone-100/50
                    // leading-relaxed (1.625 → 19.5sp) font-serif.
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Stone100.copy(alpha = 0.5f),
                    lineHeight = 19.5.sp,
                    textAlign = TextAlign.Center
                )

                if (!isDungeonMode) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        // App.tsx:2426 — max-w-xs (320px) mx-auto.
                        modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BreakDurationOption(
                            minutes = shortBreakMinutes,
                            label = "(curto)",
                            isSelected = selectedBreakMinutes == shortBreakMinutes,
                            onClick = { onSelectDuration(shortBreakMinutes) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("break_duration_short")
                        )

                        BreakDurationOption(
                            minutes = longBreakMinutes,
                            label = "(longo)",
                            isSelected = selectedBreakMinutes == longBreakMinutes,
                            onClick = { onSelectDuration(longBreakMinutes) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("break_duration_long")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 320.dp) // App.tsx:2460 — max-w-xs mx-auto
                        // Fonte: App.tsx:2463 — gradiente 3 stops
                        // from-emerald-600 via-emerald-500 to-teal-600,
                        // borda emerald-400, rounded (4px), py-3.5,
                        // tracking-widest. font-black (900) limitado a
                        // ExtraBold (800), mesmo teto do header (D-A).
                        // Sombra custom do React aproximada via spotColor.
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(4.dp),
                            spotColor = emeraldPrimary.copy(alpha = 0.3f)
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(emeraldDark, emeraldPrimary, tealDark)
                            )
                        )
                        .border(1.dp, emeraldLight, RoundedCornerShape(4.dp))
                        .clickable { onStartBreak() }
                        .padding(vertical = 14.dp)
                        .testTag("start_break_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "☕ FAZER UMA PAUSA",
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.4.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onSkipBreak,
                    modifier = Modifier.testTag("skip_break_button")
                ) {
                    Text(
                        text = "⏩ CONTINUAR SEM PAUSA",
                        fontFamily = Cinzel,
                        // Fonte: App.tsx:2470 — font-serif text-[11px]
                        // tracking-widest (0.1em = 1.1sp) text-amber-500/80.
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = Amber500.copy(alpha = 0.8f),
                        letterSpacing = 1.1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BreakEndButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Fonte: App.tsx:2751-2757 — botão "Encerrar Pausa" (gradiente emerald) com animate-pulse.
    val infiniteTransition = rememberInfiniteTransition(label = "break_end_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "break_end_pulse_alpha"
    )
    val emeraldDarkText = Color(0xFF064E3B)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = pulseAlpha }
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF34D399), Color(0xFF4ADE80), Color(0xFF6EE7B7))
                )
            )
            .border(1.dp, Color(0xFF34D399), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Encerrar Pausa",
            color = emeraldDarkText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun BreakDurationOption(
    minutes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Fonte: App.tsx:2428-2456 — botão (w-full py-2.5 text-xs border
    // font-serif rounded tracking-widest) + label span ABAIXO do botão
    // (text-[10px] text-stone-500 font-serif block text-center).
    // scale-[1.02] do selected sem equivalente no Compose — gap consciente.
    val emeraldPrimary = Color(0xFF10B981) // bg/borda selected (emerald-500)
    val emerald300 = Color(0xFF6EE7B7) // text-emerald-300 (selected)
    val backgroundColor = if (isSelected) emeraldPrimary.copy(alpha = 0.10f) else Color.Transparent
    val borderColor = if (isSelected) emeraldPrimary.copy(alpha = 0.30f) else Stone800
    val textColor = if (isSelected) emerald300 else Stone100.copy(alpha = 0.5f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                .clickable { onClick() }
                .padding(vertical = 10.dp), // py-2.5
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$minutes MIN",
                fontFamily = Cinzel,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp, // tracking-widest (0.1em)
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(4.dp)) // space-y-1
        Text(
            text = label,
            fontFamily = Cinzel,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = Stone500,
            textAlign = TextAlign.Center
        )
    }
}
