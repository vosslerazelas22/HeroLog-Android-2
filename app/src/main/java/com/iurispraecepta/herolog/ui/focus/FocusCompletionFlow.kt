package com.iurispraecepta.herolog.ui.focus

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.iurispraecepta.herolog.ui.components.FlowRowStable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.achievements.Achievement
import com.iurispraecepta.herolog.logic.focus.DroppedTitle
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.focus.LootItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono

fun getRank(isWildernessChecked: Boolean, pauseCount: Int): String = when {
    isWildernessChecked && pauseCount == 0 -> "S+"
    pauseCount == 0 -> "S"
    pauseCount == 1 -> "A"
    pauseCount <= 2 -> "B"
    pauseCount <= 4 -> "C"
    else -> "F"
}

fun getRankDescription(isWildernessChecked: Boolean, pauseCount: Int): String = when {
    isWildernessChecked && pauseCount == 0 -> "Sobrevivente Cognitivo — Lenda"
    pauseCount == 0 -> "Sem Pausas — Lendário"
    pauseCount == 1 -> "Pausa Única — Heróico"
    pauseCount <= 2 -> "Foco Estável — Exquisito"
    pauseCount <= 4 -> "Distração Parcial — Comum"
    else -> "Pausas Constantes — Instável"
}

@Composable
fun CompletionShell(
    onNext: () -> Unit,
    isLastStep: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuestPanel)
            .imePadding()
            .padding(
                start = 24.dp,
                top = 24.dp,
                end = 24.dp,
                bottom = 24.dp + LocalBottomBarInset.current
            )
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(12.dp, RoundedCornerShape(12.dp), ambientColor = Color(0xFFC29544).copy(alpha = 0.25f)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC29544),
                contentColor = Stone950
            ),
            border = BorderStroke(1.dp, Color(0xFFE9C37A))
        ) {
            Text(
                text = if (isLastStep) "RECEBER RECOMPENSAS" else "Continuar",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                fontFamily = Cinzel,
                color = Stone950
            )
        }
    }
}

@Composable
fun StreakCelebrationScreen(
    streakPreview: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .blur(24.dp)
                        .background(Color(0xFFF97316).copy(alpha = pulseAlpha), CircleShape)
                )
            }
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFF97316),
                modifier = Modifier
                    .size(80.dp)
                    .offset { IntOffset(0, bounceOffset.dp.roundToPx()) }
                    .shadow(25.dp, CircleShape, ambientColor = Color(0xFFF97316).copy(alpha = 0.6f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Você manteve a chama acesa por mais um dia",
            color = Color(0xFFD4C5A0).copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontFamily = Cinzel,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "$streakPreview ${if (streakPreview == 1) "dia" else "dias"}",
            color = Champagne300,
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            fontFamily = Cinzel,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0xFFE2B054).copy(alpha = 0.4f),
                    offset = Offset(0f, 2f),
                    blurRadius = 12f
                )
            )
        )
    }
}

@Composable
fun SessionSummaryScreen(
    rewardsCalculation: FocusRewardsCalculation,
    pauseCount: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    val rank = getRank(rewardsCalculation.isWildernessChecked, pauseCount)
    val rankDesc = getRankDescription(rewardsCalculation.isWildernessChecked, pauseCount)
    val effectiveStreak = if (streak > 0) streak else 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SESSÃO CONCLUÍDA",
            color = Champagne400,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = Cinzel,
            letterSpacing = 3.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    spotColor = Champagne400.copy(alpha = 0.35f),
                    ambientColor = Champagne400.copy(alpha = 0.35f)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "★ CLASSIFICAÇÃO $rank ★",
                color = Champagne400,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Cinzel,
                letterSpacing = 0.5.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xFFE2B054).copy(alpha = 0.35f),
                        offset = Offset(0f, 2f),
                        blurRadius = 8f
                    )
                )
            )
            Text(
                text = rankDesc.uppercase(),
                color = Amber100.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontFamily = JetBrainsMono,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(1.dp)
                .background(Amber500.copy(alpha = 0.15f))
        )
        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1C1917), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF292524), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("DURAÇÃO DA SESSÃO", color = Color(0xFF9F9F9F), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Cinzel, letterSpacing = 1.sp)
                Text("${rewardsCalculation.durationMins} MIN", color = Champagne400, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Cinzel)
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF292524)))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SEQUÊNCIA", color = Color(0xFF9F9F9F), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Cinzel, letterSpacing = 1.sp)
                Text(
                    "🔥 $effectiveStreak ${if (effectiveStreak == 1) "DIA" else "DIAS"}",
                    color = Color(0xFFF14D2A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Cinzel
                )
            }

            if (rewardsCalculation.comboBonusPercent > 0) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF292524)))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("BÔNUS DE MULTIPLICADOR COMBO", color = Color(0xFFF14D2A), fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = Cinzel, letterSpacing = 1.sp)
                    Text("+${rewardsCalculation.comboBonusPercent}%", color = Color(0xFFF14D2A), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Cinzel)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1C1917), RoundedCornerShape(12.dp))
                .border(1.dp, Amber500.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ +${rewardsCalculation.xpEarned} XP",
                    color = Color(0xFF34D399),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = JetBrainsMono,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF34D399).copy(alpha = 0.25f),
                            offset = Offset(0f, 2f),
                            blurRadius = 6f
                        )
                    )
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Amber500.copy(alpha = 0.1f))
                )
                Text(
                    text = "💎 +${rewardsCalculation.goldEarned + rewardsCalculation.dungeonClearGoldBonus} GP",
                    color = Champagne400,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = JetBrainsMono,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFFE2B054).copy(alpha = 0.25f),
                            offset = Offset(0f, 2f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun LootDropScreen(
    lootedItems: List<LootItem>,
    droppedTitle: DroppedTitle?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loot_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎁",
            fontSize = 36.sp,
            modifier = Modifier
                .offset { IntOffset(0, bounceOffset.dp.roundToPx()) }
                .shadow(15.dp, CircleShape, ambientColor = Color(0xFFA855F7).copy(alpha = 0.5f))
        )

        Text(
            text = "TESOURO CONQUISTADO",
            color = Color(0xFFC084FC),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = Cinzel,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(1.dp)
                .background(Color(0xFFA855F7).copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            lootedItems.forEach { item ->
                val isEspecial = item.rarity == Rarity.Especial
                val borderColor = if (isEspecial) Color(0xFFA855F7).copy(alpha = 0.3f) else Color(0xFF44403C)
                val badgeColor = if (isEspecial) Color(0xFFC084FC) else Color(0xFFA8A29E)
                val badgeText = if (isEspecial) "★ ESPECIAL ★" else "COMUM"
                val bgBrush = if (isEspecial) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2E1065).copy(alpha = 0.3f),
                            Stone950,
                            Stone950
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF292524).copy(alpha = 0.4f),
                            Stone950,
                            Stone950
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isEspecial) 15.dp else 0.dp, RoundedCornerShape(12.dp), ambientColor = Color(0xFFA855F7).copy(alpha = 0.1f))
                        .background(bgBrush, RoundedCornerShape(12.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    val cornerColor = if (isEspecial) Color(0xFFA855F7).copy(alpha = 0.4f) else Color(0xFF57534E).copy(alpha = 0.4f)
                    // Corner accents (React :281-284)
                    Box(modifier = Modifier.align(Alignment.TopStart).size(6.dp).border(1.dp, cornerColor, RoundedCornerShape(topStart = 2.dp)))
                    Box(modifier = Modifier.align(Alignment.TopEnd).size(6.dp).border(1.dp, cornerColor, RoundedCornerShape(topEnd = 2.dp)))
                    Box(modifier = Modifier.align(Alignment.BottomStart).size(6.dp).border(1.dp, cornerColor, RoundedCornerShape(bottomStart = 2.dp)))
                    Box(modifier = Modifier.align(Alignment.BottomEnd).size(6.dp).border(1.dp, cornerColor, RoundedCornerShape(bottomEnd = 2.dp)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.emoji,
                            fontSize = 32.sp,
                            modifier = Modifier.then(
                                if (isEspecial) Modifier.shadow(10.dp, CircleShape, ambientColor = Color(0xFFA855F7).copy(alpha = 0.4f))
                                else Modifier
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    color = if (isEspecial) Color(0xFFC4B5FD) else Color(0xFFD6D3D1),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Cinzel,
                                    letterSpacing = 0.5.sp
                                )
                                Text(text = badgeText, color = badgeColor, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono, letterSpacing = 1.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.desc,
                                color = Color(0xFFA8A29E).copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontFamily = Cinzel
                            )
                        }
                    }
                }
            }

            droppedTitle?.let { title ->
                val titleCornerColor = Color(0xFFE5C158).copy(alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(15.dp, RoundedCornerShape(12.dp), ambientColor = Color(0xFFE5C158).copy(alpha = 0.15f))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF854D0E).copy(alpha = 0.4f),
                                    Stone950,
                                    Stone950
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color(0xFFE5C158).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    // Corner accents (React :315-318)
                    Box(modifier = Modifier.align(Alignment.TopStart).size(6.dp).border(1.dp, titleCornerColor, RoundedCornerShape(topStart = 2.dp)))
                    Box(modifier = Modifier.align(Alignment.TopEnd).size(6.dp).border(1.dp, titleCornerColor, RoundedCornerShape(topEnd = 2.dp)))
                    Box(modifier = Modifier.align(Alignment.BottomStart).size(6.dp).border(1.dp, titleCornerColor, RoundedCornerShape(bottomStart = 2.dp)))
                    Box(modifier = Modifier.align(Alignment.BottomEnd).size(6.dp).border(1.dp, titleCornerColor, RoundedCornerShape(bottomEnd = 2.dp)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title.emoji,
                            fontSize = 32.sp,
                            modifier = Modifier.shadow(10.dp, CircleShape, ambientColor = Color(0xFFE5C158).copy(alpha = 0.4f))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title.name,
                                    color = Color(0xFFFCD34D),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Cinzel,
                                    letterSpacing = 0.5.sp
                                )
                                Text("★ TÍTULO RARO ★", color = Color(0xFFE5C158), fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono, letterSpacing = 1.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pode ser equipado na tela de Títulos.",
                                color = Color(0xFFA8A29E).copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontFamily = Cinzel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionNotesScreen(
    completionNotes: String,
    onNotesChange: (String) -> Unit,
    completionTag: String,
    onTagChange: (String) -> Unit,
    skillTags: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📜 CRÔNICA DA MISSÃO",
            color = Champagne400,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = Cinzel,
            letterSpacing = 2.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0xFFE2B054).copy(alpha = 0.35f),
                    offset = Offset(0f, 1f),
                    blurRadius = 8f
                )
            )
        )

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(1.dp)
                .background(Amber500.copy(alpha = 0.15f))
        )
        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "ANOTAÇÕES",
                color = Color(0xFFD4D4D8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = Cinzel,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = completionNotes,
                onValueChange = onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                placeholder = {
                    Text("O que você aprendeu ou fez nesta sessão?", color = Color(0xFF78716C), fontSize = 14.sp, fontFamily = Cinzel)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1C1917).copy(alpha = 0.85f),
                    unfocusedContainerColor = Color(0xFF1C1917).copy(alpha = 0.85f),
                    focusedBorderColor = Champagne400.copy(alpha = 0.25f),
                    unfocusedBorderColor = Color(0xFF292524),
                    focusedTextColor = Color(0xFFD4C5A0),
                    unfocusedTextColor = Color(0xFFD4C5A0)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (skillTags.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "VINCULAR SUBSKILL",
                    color = Color(0xFFD4D4D8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = Cinzel,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1917).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .border(1.dp, Amber500.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    FlowRowStable(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        skillTags.forEach { tag ->
                            val isSelected = completionTag == tag
                            val chipBg = if (isSelected) Color(0xFFD97706).copy(alpha = 0.25f) else Color(0xFF1C1917)
                            val chipBorder = if (isSelected) Color(0xFFF59E0B) else Amber500.copy(alpha = 0.05f)
                            val chipTextColor = if (isSelected) Color(0xFFFCD34D) else Color(0xFFA8A29E).copy(alpha = 0.4f)
                            val chipText = if (isSelected) "$tag ✓" else tag

                            Box(
                                modifier = Modifier
                                    .background(chipBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, chipBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (isSelected) onTagChange("") else onTagChange(tag)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = chipText,
                                    color = chipTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Cinzel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementUnlockScreen(
    achievement: Achievement,
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏆 CONQUISTA DESBLOQUEADA",
            color = Color(0xFFE5C158),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Ícone central da conquista
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(12.dp, CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFD4AF37).copy(alpha = 0.3f),
                            Color(0xFFD4AF37).copy(alpha = 0.05f)
                        )
                    )
                )
                .border(2.dp, Color(0xFFD4AF37).copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = achievement.icon, fontSize = 40.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = achievement.name,
            fontFamily = Cinzel,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFFF5F5F4),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = achievement.desc,
            fontFamily = Cinzel,
            fontSize = 13.sp,
            color = Color(0xFFA8A29E),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        if (totalCount > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${currentIndex + 1} de $totalCount",
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                color = Color(0xFF78716C)
            )
        }
    }
}

@Composable
fun FocusCompletionFlow(
    rewardsCalculation: FocusRewardsCalculation,
    pauseCount: Int,
    streak: Int,
    shouldShowStreakCelebration: Boolean,
    skillTags: List<String>,
    newAchievements: List<Achievement> = emptyList(),
    initialNotes: String = "",
    onConfirm: (editedNotes: String, selectedTag: String) -> Unit,
    initialStepIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val hasLoot = remember(rewardsCalculation) {
        rewardsCalculation.lootedItems.isNotEmpty() || rewardsCalculation.droppedTitle != null
    }

    val steps = remember(shouldShowStreakCelebration, hasLoot, newAchievements.size) {
        buildList {
            if (shouldShowStreakCelebration) add("streak")
            add("summary")
            if (hasLoot) add("loot")
            // Etapa de conquista: uma entrada por achievement desbloqueado (FR-003/US-03)
            newAchievements.forEach { _ -> add("achievement") }
            add("notes")
        }
    }

    var stepIndex by remember(initialStepIndex, steps) {
        mutableStateOf(initialStepIndex.coerceIn(0, steps.lastIndex))
    }
    var completionNotes by remember(initialNotes) { mutableStateOf(initialNotes) }
    var completionTag by remember { mutableStateOf("") }

    val streakPreview = if (shouldShowStreakCelebration) streak + 1 else streak
    val currentStep = steps.getOrElse(stepIndex) { "summary" }
    val isLastStep = stepIndex == steps.lastIndex

    // Calcular o índice da conquista atual (dentro do subconjunto de steps "achievement")
    val achievementStepsBeforeCurrent = steps.take(stepIndex).count { it == "achievement" }
    val currentAchievement = if (currentStep == "achievement" && achievementStepsBeforeCurrent < newAchievements.size) {
        newAchievements[achievementStepsBeforeCurrent]
    } else null

    CompletionShell(
        onNext = {
            if (isLastStep) {
                onConfirm(completionNotes, completionTag)
            } else {
                stepIndex += 1
            }
        },
        isLastStep = isLastStep,
        modifier = modifier
    ) {
        when (currentStep) {
            "streak" -> StreakCelebrationScreen(streakPreview = streakPreview)
            "summary" -> SessionSummaryScreen(
                rewardsCalculation = rewardsCalculation,
                pauseCount = pauseCount,
                streak = streak
            )
            "loot" -> LootDropScreen(
                lootedItems = rewardsCalculation.lootedItems,
                droppedTitle = rewardsCalculation.droppedTitle
            )
            "achievement" -> {
                if (currentAchievement != null) {
                    AchievementUnlockScreen(
                        achievement = currentAchievement,
                        currentIndex = achievementStepsBeforeCurrent,
                        totalCount = newAchievements.size
                    )
                }
            }
            "notes" -> SessionNotesScreen(
                completionNotes = completionNotes,
                onNotesChange = { completionNotes = it },
                completionTag = completionTag,
                onTagChange = { completionTag = it },
                skillTags = skillTags
            )
        }
    }
}
